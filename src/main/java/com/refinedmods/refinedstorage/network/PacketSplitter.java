package com.refinedmods.refinedstorage.network;

import com.google.common.primitives.Bytes;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketSplitter {
    private final static int MAX_PACKET_SIZE = 943718;

    private final ResourceLocation CHANNEL_ID;
    private final SimpleChannel CHANNEL;

    private static final Map<Integer, Map<Integer, byte[]>> packageCache = new HashMap<>();
    private final Map<Integer, ServerPlayerEntity> messageTargets = new HashMap<>();
    private final Map<Integer, Integer> packetMaximums = new HashMap<>();
    private final Set<Class<?>> messagesToSplit = new HashSet<>();

    private int comId = 0;
    private final int maxNumberOfMessages;
    private int ID;

    public PacketSplitter(int maxNumberOfMessages, SimpleChannel CHANNEL, ResourceLocation CHANNEL_ID) {
        this.maxNumberOfMessages = maxNumberOfMessages;
        this.CHANNEL = CHANNEL;
        this.CHANNEL_ID = CHANNEL_ID;
    }

    public boolean shouldMessageBeSplit(Class<?> clazz) {
        return messagesToSplit.contains(clazz);
    }

    public void sendToPlayer(ServerPlayerEntity player, Object message) {
        if (ID == 0) ID++; // in case we wrapped around, 0 is reserved for server
        int id = ID++;
        messageTargets.put(id, player);
        sendPacket(message, id, PacketDistributor.PLAYER.with(() -> player));
    }

    public void sendToServer(Object message) {
        messageTargets.put(0, null);
        sendPacket(message, 0, PacketDistributor.SERVER.noArg());
    }

    //@Volatile mostly copied from SimpleChannel
    private void sendPacket(Object Message, int id, PacketDistributor.PacketTarget target) {
        final PacketBuffer bufIn = new PacketBuffer(Unpooled.buffer());

        //write the message id to be able to figure out where the packet is supposed to go in the wrapper
        bufIn.writeInt(id);

        int index = CHANNEL.encodeMessage(Message, bufIn);
        target.send(target.getDirection().buildPacket(Pair.of(bufIn, index), CHANNEL_ID).getThis());
    }

    public <MSG> void registerMessage(int index, Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        registerMessage(index, maxNumberOfMessages, messageType, encoder, decoder, messageConsumer);
    }

    public <MSG> void registerMessage(int index, int maxNumberOfMessages, Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        packetMaximums.put(index, maxNumberOfMessages);
        messagesToSplit.add(messageType);

        BiConsumer<MSG, PacketBuffer> wrappedEncoder = (msg, buffer) -> {
            int id = buffer.readInt();
            buffer.discardReadBytes();
            ServerPlayerEntity player = messageTargets.get(id);
            messageTargets.remove(id);

            //write a zero for the number of packets in case the packet does not need to be split
            buffer.writeShort(0);
            encoder.accept(msg, buffer);
            createSplittingConsumer(player).accept(msg, buffer);
        };

        CHANNEL.registerMessage(index, messageType, wrappedEncoder, createPacketCombiner().andThen(decoder), messageConsumer);
    }

    private <MSG> BiConsumer<MSG, PacketBuffer> createSplittingConsumer(ServerPlayerEntity playerEntity) {
        return (MSG, buf) -> {

            if (buf.writerIndex() < MAX_PACKET_SIZE) {
                return;
            }

            //read packetId for this packet
            int packetId = buf.readUnsignedByte();

            //this short is written here in case we are not splitting, ignore for split packages
            buf.readShort();

            //ignore the above as it is not required for the final packet
            int currentIndex = buf.readerIndex();
            int packetIndex = 0;
            final int comId = this.comId++;

            //Data for this packet
            byte[] packetData = new byte[0];

            int maximumPackets = packetMaximums.get(packetId);
            int expectedPackets = buf.writerIndex() / MAX_PACKET_SIZE + 1;
            boolean failure = false;

            //Loop while data is available.
            while (currentIndex < buf.writerIndex()) {

                int sliceSize = Math.min(MAX_PACKET_SIZE, buf.writerIndex() - currentIndex);

                //Extract the sub data array.
                byte[] subPacketData = Arrays.copyOfRange(buf.array(), currentIndex, currentIndex + sliceSize);

                if (packetIndex == 0) { // Assign Data for first Packet to this packet.
                    packetData = subPacketData;
                    packetIndex++;
                } else {
                    //Construct the split packet.
                    SplitPacketMessage splitPacketMessage = new SplitPacketMessage(comId, packetIndex++, subPacketData);

                    if (playerEntity == null) {
                        CHANNEL.send(PacketDistributor.SERVER.noArg(), splitPacketMessage);
                    } else {
                        CHANNEL.send(PacketDistributor.PLAYER.with(() -> playerEntity), splitPacketMessage);
                    }
                }

                //Move our working index.
                currentIndex += sliceSize;

                if (packetIndex > maximumPackets) {
                    LogManager.getLogger().error("Failure Splitting Packets on Channel \"" + CHANNEL_ID + "\"." + " with " + MSG.getClass() + ". " +
                        " Number of Packets sent " + (packetIndex - 1) + ", expected number of Packets " + expectedPackets + ", maximum number of packets for a message of this type " + packetMaximums.get(packetId));
                    failure = true;
                    break;
                }
            }

            //start writing at the beginning
            buf.setIndex(0, 0);

            //packetId is required for forge to match the packet
            buf.writeByte(packetId);

            //number of packets the packet was split into
            buf.writeShort(failure ? expectedPackets : packetIndex);
            buf.writeInt(comId);
            buf.writeByteArray(packetData);

            //copies the written data into a new buffer discarding the old one
            buf.capacity(buf.writerIndex());
        };
    }

    private Function<PacketBuffer, PacketBuffer> createPacketCombiner() {
        return (buf) -> {
            int size = buf.readShort();

            //This packet was not split
            if (size < 2) return buf;

            int comId = buf.readInt();

            Map<Integer, byte[]> partsMap = packageCache.get(comId);
            if (partsMap == null || partsMap.size() != size - 1) {
                int partSize = partsMap == null ? 0 : partsMap.size();
                int id = buf.readUnsignedByte();
                int max = packetMaximums.get(id) == null ? 0 : packetMaximums.get(id);
                throw new PacketSplittingException(CHANNEL_ID, partSize, size, max, id);
            }

            //Add data that came from this packet
            addPackagePart(comId, 0, buf.readByteArray());

            //Combine Cached Data
            final byte[] packetData = partsMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .reduce(new byte[0], Bytes::concat);

            PacketBuffer buffer = new PacketBuffer(Unpooled.wrappedBuffer(packetData));

            //remove data from cache
            packageCache.remove(comId);
            return buffer;
        };
    }

    public void addPackagePart(int communicationId, int packetIndex, byte[] payload) {
        //Sync on the message cache since this is still on the Netty thread.
        synchronized (PacketSplitter.packageCache) {
            PacketSplitter.packageCache.computeIfAbsent(communicationId, (id) -> new ConcurrentHashMap<>());
            PacketSplitter.packageCache.get(communicationId).put(packetIndex, payload);
        }
    }
}

class PacketSplittingException extends RuntimeException {
    ResourceLocation channnelId;
    int actualSize;
    int expectedSize;
    int maximumSize;
    int packetId;

    public PacketSplittingException(ResourceLocation channnelId, int actualSize, int expectedSize, int maximumSize, int packetId) {
        this.channnelId = channnelId;
        this.actualSize = actualSize;
        this.expectedSize = expectedSize;
        this.maximumSize = maximumSize;
        this.packetId = packetId;
    }

    @Override
    public String getMessage() {
        return "Failure Splitting Packets on Channel \"" + channnelId.toString() + "\"." +
            " Number of Packets sent " + actualSize + ", Number of Packets expected " + expectedSize + ", maximum number of packets for a message of this type " + maximumSize;
    }

}
