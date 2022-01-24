package io.github.orlouge.anvilcharge;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class AnvilChargeMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("anvilcharge");
	public static final String CONFIG_FNAME = "config/anvilcharge.properties";

	public static boolean CONSUME_ANVIL = true;
	public static long ENERGY_PER_XP = 2000;

	@Override
	public void onInitialize() {
		Properties defaultProps = new Properties();

		defaultProps.setProperty("energy_per_xp", Long.toString(ENERGY_PER_XP));
		defaultProps.setProperty("consume_anvil", Boolean.toString(CONSUME_ANVIL));

		File f = new File(CONFIG_FNAME);
		if (f.isFile() && f.canRead()) {
			try (FileInputStream in = new FileInputStream(f)) {
				Properties props = new Properties(defaultProps);
				props.load(in);
				ENERGY_PER_XP = Long.parseLong(props.getProperty("energy_per_xp"));
				CONSUME_ANVIL = Boolean.parseBoolean(props.getProperty("consume_anvil"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try (FileOutputStream out = new FileOutputStream(CONFIG_FNAME)) {
				defaultProps.store(out, "");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
