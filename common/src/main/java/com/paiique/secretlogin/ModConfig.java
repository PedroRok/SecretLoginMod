package com.paiique.secretlogin;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    private List<String> commandFilter;

    private void init() {
        commandFilter = new ArrayList<>();

        try {
            File file = new File("config/secretlogin-commands.txt");
            if (!file.exists()) copyDefaultConfig();

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();

            while (line != null) {
                commandFilter.add(line + " ");
                line = reader.readLine();
            }
            reader.close();

        } catch (IOException e) {
            throw new RuntimeException("Error reading SecretLogin commands list", e);
        }
    }

    private void copyDefaultConfig() {
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(Secretlogin.MOD_ID, "config/secretlogin-commands.txt");
        try (InputStream inputStream = Minecraft.getInstance().getResourceManager().getResource(resourceLocation).orElseGet(null).open()) {
            FileOutputStream outputStream = new FileOutputStream("config/secretlogin-commands.txt");
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Error reading SecretLogin default commands list", e);
        }
    }

    public List<String> getCommandFilter() {
        if (commandFilter == null) init();
        return commandFilter;
    }
}
