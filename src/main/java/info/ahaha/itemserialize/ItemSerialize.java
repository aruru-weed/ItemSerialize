package info.ahaha.itemserialize;

import com.google.common.io.BaseEncoding;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
/*

    public static ItemStack deserializeItemStack(String data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());

        NBTTagCompound nbtTagCompoundRoot = null;
        try {
        nbtTagCompoundRoot = NBTCompressedStreamTools.a((DataInput) new DataInputStream(inputStream));
        } catch (IOException e) {
        e.printStackTrace();
        }

        net.minecraft.server.v1_16_R3.ItemStack nmsItem = net.minecraft.server.v1_16_R3.ItemStack.a(nbtTagCompoundRoot);

        ItemStack item = (ItemStack) CraftItemStack.asBukkitCopy(nmsItem);
        return item;
        }

 */
public final class ItemSerialize {
    public static ItemStack deserializeItemStack(String data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());

        ItemStack itemStack = null;
        try {
            Class<?> nbtTagCompoundClass = getNMSClass("NBTTagCompound");

            Object nbtTagCompound = getNMSClass("NBTCompressedStreamTools").getMethod("a", InputStream.class).invoke(null, inputStream);

            // net.minecraft.server.v1_16_R3.ItemStack nmsItem = net.minecraft.server.v1_16_R3.ItemStack.a(nbtTagCompoundRoot);
            Class<?> nmsItemStackClass = getNMSClass("ItemStack");
            Object craftItemStack = nmsItemStackClass.getMethod("a", nbtTagCompoundClass).invoke(null, nbtTagCompound);

            itemStack = (ItemStack) getOBClass("inventory.CraftItemStack").getMethod("asBukkitCopy", nmsItemStackClass).invoke(null, craftItemStack);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }

        return itemStack;
    }
/*
public static String serializeItemStack(ItemStack item) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        NBTTagList nbtTagListItems = new NBTTagList();
        NBTTagCompound nbtTagCompoundItem = new NBTTagCompound();

        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        nmsItem.save(nbtTagCompoundItem);

        nbtTagListItems.add(nbtTagCompoundItem);

        try {
            NBTCompressedStreamTools.a(nbtTagCompoundItem, (DataOutput) dataOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }
 */
    public static String serializeItemStack(ItemStack item) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        try {
            // NBTTagCompound nbtTagCompoundItem = new NBTTagCompound();
            Class<?> nbtTagCompoundClass = getNMSClass("NBTTagCompound");
            Constructor<?> nbtTagCompoundConstructor = nbtTagCompoundClass.getConstructor();
            Object nbtTagCompound = nbtTagCompoundConstructor.newInstance();

            // NBTTagList nbtTagListItems = new NBTTagList();
            Class<?> nbtTagListItemsClass = getNMSClass("NBTTagList");
            Constructor<?> nbtTagListItemsConstructor = nbtTagCompoundClass.getConstructor();
            Object nbtTagListItems = nbtTagCompoundConstructor.newInstance();

            // net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
            Object nmsItemStack = getOBClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            // nmsItem.save(nbtTagCompoundItem);
            getNMSClass("ItemStack").getMethod("save", nbtTagCompoundClass).invoke(nmsItemStack, nbtTagCompound);

            // NBTCompressedStreamTools.a(nbtTagCompoundItem, (DataOutput) dataOutput);
            //Object a = getNMSClass("NBTCompressedStreamTools").getField("a").get(null);
            getNMSClass("NBTCompressedStreamTools").getMethod("a", nbtTagCompoundClass, DataOutput.class).invoke(null, nbtTagCompound, (DataOutput) dataOutput);

        } catch (SecurityException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
//        return BaseEncoding.base64().encode(outputStream.toByteArray());
        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    private static Class<?> getNMSClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch (ClassNotFoundException var3) {
            var3.printStackTrace();
            return null;
        }
    }

    private static Class<?> getOBClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
        } catch (ClassNotFoundException var3) {
            var3.printStackTrace();
            return null;
        }
    }
}
