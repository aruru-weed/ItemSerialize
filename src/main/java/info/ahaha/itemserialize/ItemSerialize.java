package info.ahaha.itemserialize;

import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.math.BigInteger;

public final class ItemSerialize {
    public static String serializeItemStack(ItemStack item) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        NBTTagList nbtTagListItems = new NBTTagList();
        NBTTagCompound nbtTagCompoundItem = new NBTTagCompound();

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        nmsItem.save(nbtTagCompoundItem);

        nbtTagListItems.add(nbtTagCompoundItem);

        try {
            NBTCompressedStreamTools.a(nbtTagCompoundItem, (DataOutput) dataOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    public static ItemStack deserializeItemStack(String data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());

        NBTTagCompound nbtTagCompoundRoot = null;
        try {
            nbtTagCompoundRoot = NBTCompressedStreamTools.a((DataInput) new DataInputStream(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }

        net.minecraft.world.item.ItemStack nmsItem = net.minecraft.world.item.ItemStack.a(nbtTagCompoundRoot);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }
}
