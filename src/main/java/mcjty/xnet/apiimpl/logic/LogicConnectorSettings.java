package mcjty.xnet.apiimpl.logic;

import com.google.common.collect.ImmutableSet;
import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.api.gui.IndicatorIcon;
import mcjty.xnet.api.helper.AbstractConnectorSettings;
import mcjty.xnet.blocks.controller.gui.GuiController;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LogicConnectorSettings extends AbstractConnectorSettings {

    public static final String TAG_MODE = "mode";
    public static final String TAG_SPEED = "speed";
    public static final String TAG_REDSTONE_OUT = "rsout";

    enum LogicMode {
        SENSOR,
        OUTPUT
    }


    public static final int SENSORS = 4;

    private LogicMode logicMode = LogicMode.SENSOR;
    private List<Sensor> sensors = null;

    private int colors;         // Current colormask
    private int speed = 2;
    private Integer redstoneOut;    // Redstone output value

    public LogicConnectorSettings(@Nonnull EnumFacing side) {
        super(side);
        sensors = new ArrayList<>(SENSORS);
        for (int i = 0 ; i < SENSORS ; i++) {
            sensors.add(new Sensor(i));
        }
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setColorMask(int colors) {
        this.colors = colors;
    }

    public int getColorMask() {
        return colors;
    }

    public Integer getRedstoneOut() {
        return redstoneOut;
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        switch (logicMode) {
            case SENSOR:
                return new IndicatorIcon(GuiController.iconGuiElements, 26, 70, 13, 10);
            case OUTPUT:
                return new IndicatorIcon(GuiController.iconGuiElements, 39, 70, 13, 10);
        }
        return null;
    }



    @Nullable
    @Override
    public String getIndicator() {
        return null;
    }

    private static Set<String> TAGS = ImmutableSet.of(TAG_REDSTONE_OUT, TAG_MODE, TAG_RS, TAG_COLOR+"0", TAG_COLOR+"1", TAG_COLOR+"2", TAG_COLOR+"3");

    @Override
    public boolean isEnabled(String tag) {
        if (tag.equals(TAG_FACING)) {
            return advanced;
        }
        if (tag.equals(TAG_SPEED)) {
            return true;
        }
        for (Sensor sensor : sensors) {
            if (sensor.isEnabled(tag)) {
                return true;
            }
        }

        return TAGS.contains(tag);
    }

    public int getSpeed() {
        return speed;
    }

    public LogicMode getLogicMode() {
        return logicMode;
    }


    @Override
    public void createGui(IEditorGui gui) {
        advanced = gui.isAdvanced();
        String[] speeds;
        if (advanced) {
            speeds = new String[] { "5", "10", "20", "60", "100", "200" };
        } else {
            speeds = new String[] { "10", "20", "60", "100", "200" };
        }
        sideGui(gui);
        colorsGui(gui);
        redstoneGui(gui);
        gui.nl()
                .choices(TAG_MODE, "Sensor or Output mode", logicMode, LogicMode.values())
                .choices(TAG_SPEED, (logicMode == LogicMode.SENSOR ? "Number of ticks for each check" : "Number of ticks for each operation"), Integer.toString(speed * 5), speeds)
                .nl();
        if (logicMode == LogicMode.SENSOR) {
            for (Sensor sensor : sensors) {
                sensor.createGui(gui);
            }
        } else {
            gui.label("Redstone:")
                    .integer(TAG_REDSTONE_OUT, "Redstone output value", redstoneOut, 40, 16)
                    .nl();
        }
    }

    @Override
    public void update(Map<String, Object> data) {
        super.update(data);
        logicMode = LogicMode.valueOf(((String)data.get(TAG_MODE)).toUpperCase());
        String facing = (String) data.get(TAG_FACING);
        // @todo suspicious

        speed = Integer.parseInt((String) data.get(TAG_SPEED)) / 5;
        if (speed == 0) {
            speed = 2;
        }
        if (logicMode == LogicMode.SENSOR) {
            for (Sensor sensor : sensors) {
                sensor.update(data);
            }
        } else {
            redstoneOut = (Integer) data.get(TAG_REDSTONE_OUT);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        logicMode = LogicMode.values()[tag.getByte("logicMode")];
        speed = tag.getInteger("speed");
        if (speed == 0) {
            speed = 2;
        }
        colors = tag.getInteger("colors");
        for (Sensor sensor : sensors) {
            sensor.readFromNBT(tag);
        }
        redstoneOut = tag.getInteger("rsout");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setByte("logicMode", (byte) logicMode.ordinal());
        tag.setInteger("speed", speed);
        tag.setInteger("colors", colors);
        for (Sensor sensor : sensors) {
            sensor.writeToNBT(tag);
        }
        if (redstoneOut != null) {
            tag.setInteger("rsout", redstoneOut);
        }
    }

}
