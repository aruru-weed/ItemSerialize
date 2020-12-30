package info.ahaha.itemserialize;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public final class ItemSerializer implements Serializable {
    public ItemSerializer(ItemStack... items) {
        try {
            ser = new serializer();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        try {
            des = new deserializer();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        addItem(items);
    }

    serializer ser;
    deserializer des;
    List<String> list = new ArrayList<>();

    public void addItem(ItemStack... items) {
        for (ItemStack item : items) {
            try {
                list.add(ser.serializeItemStack(item));
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    public List<ItemStack> getItems() {
        List<ItemStack> list = new ArrayList<>();
        for (String str : this.list) {
            try {
                list.add(des.deserializeItemStack(str));
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static class deserializer {
        public deserializer() throws NoSuchMethodException {
            Class<?> nbtTagCompoundClass = getNMSClass("NBTTagCompound");
            nmsItemStackClass = getNMSClass("ItemStack");
            nbtCompressedStreamToolsMethod_a = getNMSClass("NBTCompressedStreamTools").getMethod("a", DataInput.class);
            nmsItemStackMethod_a = nmsItemStackClass.getMethod("a", nbtTagCompoundClass);
            craftItemStackMethod_asBukkitCopy = getCraftClass("inventory.CraftItemStack").getMethod("asBukkitCopy", nmsItemStackClass);
        }

        Class<?> nmsItemStackClass;
        Method nbtCompressedStreamToolsMethod_a;
        Method nmsItemStackMethod_a;
        Method craftItemStackMethod_asBukkitCopy;

        public ItemStack deserializeItemStack(String data) throws InvocationTargetException, IllegalAccessException {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());

            Object nbtTagCompound = nbtCompressedStreamToolsMethod_a.invoke(null, new DataInputStream(inputStream));
            Object craftItemStack = nmsItemStackMethod_a.invoke(null, nbtTagCompound);
            return (ItemStack) craftItemStackMethod_asBukkitCopy.invoke(null, craftItemStack);
        }
    }

    public static class serializer {
        public serializer() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            Class<?> nbtTagCompoundClass = getNMSClass("NBTTagCompound");
            nbtTagCompoundConstructor = nbtTagCompoundClass.getConstructor();
            craftItemStackMethod_asNMSCopy = getCraftClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class);
            nmsItemStackMethod_save = getNMSClass("ItemStack").getMethod("save", nbtTagCompoundClass);
            nbtCompressedStreamTools_a = getNMSClass("NBTCompressedStreamTools").getMethod("a", nbtTagCompoundClass, DataOutput.class);
        }

        Method craftItemStackMethod_asNMSCopy;
        Method nmsItemStackMethod_save;
        Constructor<?> nbtTagCompoundConstructor;
        Method nbtCompressedStreamTools_a;

        public String serializeItemStack(ItemStack item) throws InvocationTargetException, IllegalAccessException, InstantiationException {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutput = new DataOutputStream(outputStream);

            Object nmsItemStack = craftItemStackMethod_asNMSCopy.invoke(null, item);
            Object nbtTagCompound = nbtTagCompoundConstructor.newInstance();
            nmsItemStackMethod_save.invoke(nmsItemStack, nbtTagCompound);

            nbtCompressedStreamTools_a.invoke(null, nbtTagCompound, (DataOutput) dataOutput);
            return new BigInteger(1, outputStream.toByteArray()).toString(32);
        }
    }

    public static ItemStack deserializeItemStack(String data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());

        ItemStack itemStack = null;
        try {
            Class<?> nbtTagCompoundClass = getNMSClass("NBTTagCompound");

            Object nbtTagCompound = getNMSClass("NBTCompressedStreamTools").getMethod("a", DataInput.class).invoke(null, new DataInputStream(inputStream));

            Class<?> nmsItemStackClass = getNMSClass("ItemStack");
            Object craftItemStack = nmsItemStackClass.getMethod("a", nbtTagCompoundClass).invoke(null, nbtTagCompound);

            itemStack = (ItemStack) getCraftClass("inventory.CraftItemStack").getMethod("asBukkitCopy", nmsItemStackClass).invoke(null, craftItemStack);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }

        return itemStack;
    }

    public static String serializeItemStack(ItemStack item) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        try {
            Class<?> nbtTagCompoundClass = getNMSClass("NBTTagCompound");
            Constructor<?> nbtTagCompoundConstructor = nbtTagCompoundClass.getConstructor();
            Object nbtTagCompound = nbtTagCompoundConstructor.newInstance();

            Object nmsItemStack = getCraftClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            getNMSClass("ItemStack").getMethod("save", nbtTagCompoundClass).invoke(nmsItemStack, nbtTagCompound);

            getNMSClass("NBTCompressedStreamTools").getMethod("a", nbtTagCompoundClass, DataOutput.class).invoke(null, nbtTagCompound, (DataOutput) dataOutput);

        } catch (SecurityException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
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

    private static Class<?> getCraftClass(String name) {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
        } catch (ClassNotFoundException var3) {
            var3.printStackTrace();
            return null;
        }
    }
}
