package net.pixeldreamstudios.iconleadingtooltip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IconLeadingTooltip {
    public static final String MOD_ID = "icon-leading-tooltip";
    public static final String MOD_ID_NEOFORGE = "icon_leading_tooltip";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        LOGGER.info("Icon Leading Tooltip initialized!");
    }
}