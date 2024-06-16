package com.sucy.skill.api.particle;

public enum ParticleType {
    BARRIER("barrier"),
    BLOCK_CRACK("block crack", "blackcrack_", true),
    CLOUD("cloud", "cloud"),
    CRIT("crit", "crit"),
    CRIT_MAGIC("magic crit", "magicCrit"),
    DAMAGE_INDICATOR("damage indicator"),
    DRAGON_BREATH("dragon breath"),
    DRIP_LAVA("drip lava", "dripLava"),
    DRIP_WATER("drip water", "dripWater"),
    ENCHANTMENT_TABLE("enchantment table", "enchantmenttable"),
    END_ROD("end rod"),
    EXPLOSION_HUGE("huge explosion", "hugeexplosion"),
    EXPLOSION_LARGE("large explode", "largeexplode"),
    EXPLOSION_NORMAL("explode", "explode"),
    FIREWORKS_SPARK("firework spark", "fireworksSpark"),
    FLAME("flame", "flame"),
    FOOTSTEP("footstep", "footstep"),
    HEART("heart", "heart"),
    LAVA("lava", "lava"),
    MOB_APPEARANCE("mob appearance"),
    NOTE("note", "note"),
    PORTAL("portal", "portal"),
    REDSTONE("red dust", "reddust"),
    SLIME("slime", "slime"),
    SMOKE_NORMAL("smoke", "smoke"),
    SMOKE_LARGE("large smoke", "largesmoke"),
    SNOWBALL("snowball poof", "snowballpoof"),
    SNOW_SHOVEL("snow shovel", "snowshovel"),
    SPELL("spell", "spell"),
    SPELL_INSTANT("instant spell", "instantSpell"),
    SPELL_MOB("mob spell", "mobSpell"),
    SPELL_MOB_AMBIENT("mob spell ambient", "mobSpellAmbient"),
    SPELL_WITCH("witch magic", "witchMagic"),
    SUSPEND_DEPTH("depth suspend", "depthSuspend"),
    SUSPENDED("suspend", "suspend"),
    SWEEP_ATTACK("sweep attack"),
    TOWN_AURA("town aura", "townaura"),
    VILLAGER_ANGRY("angry villager", "angryVillager"),
    VILLAGER_HAPPY("happy villager", "happyVillager"),
    WATER_BUBBLE("bubble", "bubble"),
    WATER_SPLASH("splash", "splash"),
    WATER_WAKE("water wake");

    private String  editor;
    private String  old;
    private boolean mat;

    ParticleType(String editorKey)
    {
        editor = editorKey;
        ParticleLookup.register(this);
    }

    ParticleType(String editorKey, String oldName)
    {
        editor = editorKey;
        old = oldName;
        ParticleLookup.register(this);
    }

    ParticleType(String editorKey, String oldName, boolean usesMat)
    {
        this(editorKey, oldName);
        mat = usesMat;
    }

    public String editorKey()
    {
        return editor;
    }

    public String oldName()
    {
        return old;
    }

    public boolean usesMat()
    {
        return mat;
    }
}
