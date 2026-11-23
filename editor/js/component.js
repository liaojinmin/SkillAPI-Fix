var hoverSpace;

var DAMAGE_TYPES = [ 'Block Explosion', 'Contact', 'Cramming', 'Dragon Breath', 'Drowning', 'Entity Attack', 'Entity Explosion', 'Fall', 'Falling Block', 'Fire', 'Fire Tick', 'Fly Into Wall', 'Hot Floor', 'Lava', 'Lightning', 'Magic', 'Melting', 'Poison', 'Projectile', 'Starvation', 'Suffocation', 'Suicide', 'Thorns', 'Void', 'Wither' ];

function canDrop(thing, target) {
    if (thing === target) return false;
    
    var temp = target;
    while (temp.parentNode) {
        temp = temp.parentNode;
        if (temp === thing) return false;
    }
    return true;
}

/**
 * Types of components
 */
var Type = {
    TRIGGER   : 'trigger',
    TARGET    : 'target',
    CONDITION : 'condition',
    MECHANIC  : 'mechanic'
};

/**
 * Available triggers for activating skill effects
 */
var Trigger = {
    BLOCK_BREAK          : { name: 'Block Break',          container: true, construct: TriggerBlockBreak,        premium: true },
    BLOCK_PLACE          : { name: 'Block Place',          container: true, construct: TriggerBlockPlace,        premium: true },
    CAST                 : { name: 'Cast',                 container: true, construct: TriggerCast               },
    CLEANUP              : { name: 'Cleanup',              container: true, construct: TriggerCleanup            },
    CROUCH               : { name: 'Crouch',               container: true, construct: TriggerCrouch             },
    DEATH                : { name: 'Death',                container: true, construct: TriggerDeath              },
    ENVIRONMENT_DAMAGE   : { name: 'Environment Damage',   container: true, construct: TriggerEnvironmentDamage, premium: true },
    INITIALIZE           : { name: 'Initialize',           container: true, construct: TriggerInitialize         },
    KILL                 : { name: 'Kill',                 container: true, construct: TriggerKill               },
    LAND                 : { name: 'Land',                 container: true, construct: TriggerLand               },
    LAUNCH               : { name: 'Launch',               container: true, construct: TriggerLaunch             },
    MOVE                 : { name: 'Move',                 container: true, construct: TriggerMove,              premium: true },
    PHYSICAL_DAMAGE      : { name: 'Physical Damage',      container: true, construct: TriggerPhysicalDamage     },
    SKILL_DAMAGE         : { name: 'Skill Damage',         container: true, construct: TriggerSkillDamage        },
    TOOK_PHYSICAL_DAMAGE : { name: 'Took Physical Damage', container: true, construct: TriggerTookPhysicalDamage },
    TOOK_SKILL_DAMAGE    : { name: 'Took Skill Damage',    container: true, construct: TriggerTookSkillDamage    },
    MYTHIC_SUMMON_DEATH    : { name: 'Mythic Summon Death',    container: true, construct: TriggerMythicSummonDeath    },
    PLAYER_TEAM_START    : { name: 'Player Team Start',    container: true, construct: TriggerPlayerTeamStart,   premium: true },
    PLAYER_ARENA_START    : { name: 'Player Arena Start',    container: true, construct: TriggerPlayerArenaStart,  premium: true},
    PLAYER_KILLER_ENTITY    : { name: 'Player Killer Entity',    container: true, construct: TriggerPlayerKillerEntity,  premium: true}, // 2025/11/30
    PLAYER_HEALTH_RESTORE  : { name: 'Player Health Restore',    container: true, construct: TriggerPlayerHealthRestore,  premium: true}, // 2025/11/30
    SKILL_LINK  : { name: 'Skill Link',                    container: true, construct: TriggerSkillLink,  premium: true}, // 2025/12/1
    TRAIL_HURT  : { name: 'Trail Hurt',                    container: true, construct: TriggerTrailHurt,  premium: true}, // 2026/01/19

    COMBAT_ENTER  : { name: 'Combat Enter',                    container: true, construct: TriggerCombatEnter,  premium: true}, // 2026/01/28
    COMBAT_LEAVE  : { name: 'Combat Leave',                    container: true, construct: TriggerCombatLeave,  premium: true},  // 2026/01/28
    ATTRIBUTE_CHANGE  : { name: 'Attribute Change',                    container: true, construct: TriggerAttributeChange,  premium: true},  // 2026/02/1
    KNEE : { name: "Knee",  container:  true, construct: TriggerKnee, premium: true }, // 2026/3/18
    KNEE_END : { name: "Knee End",  container:  true, construct: TriggerEndKnee, premium: true }, // 2026/3/18
    USE_CONSUME : { name: "Use Consume",  container:  true, construct: TriggerPlayerUseConsume, premium: true }, // 2026/4/5
    BOUNCE_COLLISION : { name: "Bounce Collision",  container:  true, construct: TriggerBounceCollision, premium: true }, // 2026/11/24
    BOUNCE_SPLIT : { name: "Bounce Split",  container:  true, construct: TriggerBounceSplit, premium: true }, // 2026/11/24
    BOUNCE_END : { name: "Bounce End",  container:  true, construct: TriggerBounceEnd, premium: true } // 2026/11/24
};

/**
 * Available target component data
 */ 
var Target = {
    AREA     : { name: 'Area',     container: true, construct: TargetArea     },
    CONE     : { name: 'Cone',     container: true, construct: TargetCone     },
    LINEAR   : { name: 'Linear',   container: true, construct: TargetLinear   },
    LOCATION : { name: 'Location', container: true, construct: TargetLocation },
    RANDOM_LOCATION : { name: 'Random Location', container: true, construct: TargetRandomLocation },
    NEAREST  : { name: 'Nearest',  container: true, construct: TargetNearest  },
    OFFSET   : { name: 'Offset',   container: true, construct: TargetOffset   },
    REMEMBER : { name: 'Remember', container: true, construct: TargetRemember },
    SELF     : { name: 'Self',     container: true, construct: TargetSelf     },
    SINGLE   : { name: 'Single',   container: true, construct: TargetSingle   },
    CONTEXT_ARMOR_STAND   : { name: 'ContextArmorStand',   container: true, construct: TargetContextArmorStand   },
    SUMMON_OWNER     : { name: 'Summon Owner',     container: true, construct: TargetSummonOwner     },
    TEAM_PLAYER     : { name: 'Team Player',     container: true, construct: TargetTeamPlayer    }
};

/**
 * Available condition component data
 */ 
var Condition = {
    ARMOR:       { name: 'Armor',       container: true, construct: ConditionArmor      },
    ATTRIBUTE:   { name: 'Attribute',   container: true, construct: ConditionAttribute  },
    BIOME:       { name: 'Biome',       container: true, construct: ConditionBiome      },
    BLOCK:       { name: 'Block',       container: true, construct: ConditionBlock      },
    CEILING:     { name: 'Ceiling',     container: true, construct: ConditionCeiling,   premium: true },
    CHANCE:      { name: 'Chance',      container: true, construct: ConditionChance     },
    CLASS:       { name: 'Class',       container: true, construct: ConditionClass      },
    CLASS_LEVEL: { name: 'Class Level', container: true, construct: ConditionClassLevel },
    COMBAT:      { name: 'Combat',      container: true, construct: ConditionCombat     },
    CROUCH:      { name: 'Crouch',      container: true, construct: ConditionCrouch     },
    DIRECTION:   { name: 'Direction',   container: true, construct: ConditionDirection  },
    ELEVATION:   { name: 'Elevation',   container: true, construct: ConditionElevation  },
    ELSE:        { name: 'Else',        container: true, construct: ConditionElse,      premium: true },
    ENTITY_TYPE: { name: 'Entity Type', container: true, construct: ConditionEntityType,premium: true },
    ENTITY_CATEGORY: { name: 'Entity Category', container: true, construct: ConditionEntityCategory,premium: true },
    ENTITY_EXCLUDE: { name: 'Entity Exclude', container: true, construct: ConditionEntityExclude,premium: true },
    FIRE:        { name: 'Fire',        container: true, construct: ConditionFire       },
    FLAG:        { name: 'Flag',        container: true, construct: ConditionFlag       },
    GROUND:      { name: 'Ground',      container: true, construct: ConditionGround,    premium: true },
    HEALTH:      { name: 'Health',      container: true, construct: ConditionHealth     },
    INVENTORY:   { name: 'Inventory',   container: true, construct: ConditionInventory  },
    ITEM:        { name: 'Item',        container: true, construct: ConditionItem       },
    LIGHT:       { name: 'Light',       container: true, construct: ConditionLight      },
    MANA:        { name: 'Mana',        container: true, construct: ConditionMana       },
    NAME:        { name: 'Name',        container: true, construct: ConditionName       },
    OFFHAND:     { name: 'Offhand',     container: true, construct: ConditionOffhand    },
    PERMISSION:  { name: 'Permission',  container: true, construct: ConditionPermission,premium: true },
    POTION:      { name: 'Potion',      container: true, construct: ConditionPotion     },
    SKILL_LEVEL: { name: 'Skill Level', container: true, construct: ConditionSkillLevel },
    SHIELD:      { name: 'Shield',      container: true, construct: ConditionShield     },
    SLOT:        { name: 'Slot',        container: true, construct: ConditionSlot,      premium: true },
    STATUS:      { name: 'Status',      container: true, construct: ConditionStatus     },
    TIME:        { name: 'Time',        container: true, construct: ConditionTime       },
    TOOL:        { name: 'Tool',        container: true, construct: ConditionTool       },
    VALUE:       { name: 'Value',       container: true, construct: ConditionValue      },
    WATER:       { name: 'Water',       container: true, construct: ConditionWater      },
    WEATHER:     { name: 'Weather',     container: true, construct: ConditionWeather,   premium: true },
    DATA:        { name: 'Data',        container: true, construct: ConditionData,      premium: true },
    WORLD:       { name: 'World',       container: true, construct: ConditionWorld,     premium: true},
    AREA_ENTITY:       { name: 'Area Entity',       container: true, construct: ConditionAreaEntity,     premium: true }, // 2025/11/30
    HEALTH_SCALE:       { name: 'Health Scale',       container: true, construct: ConditionHealthScale,     premium: true }, // 2025/11/30
    HEALTH_COMPARISON:       { name: 'Health Comparison',       container: true, construct: ConditionHealthComparison,     premium: true }, // 2025/11/30
    HEALTH_INTERVAL:       { name: 'Health Interval',       container: true, construct: ConditionHealthInterval,     premium: true }, // 2026/01/11
    MYTHIC_SUMMON_SURVIVAL:       { name: 'Mythic Summon Survival',       container: true, construct: ConditionMythicSummonSurvival,     premium: true}
};

/**
 * Available mechanic component data
 */
var Mechanic = {
    ATTRIBUTE:           { name: 'Attribute',           container: false, construct: MechanicAttribute          },
    BLOCK_WALL:          { name: 'Block Wall',          container: false, construct: MechanicBlockWall          },
    BLOCK:               { name: 'Block',               container: false, construct: MechanicBlock              },
    BOUNCE_PROJECTILE:   { name: 'Bounce Projectile',   container: true,  construct: MechanicBounceProjectile   },
    BUFF:                { name: 'Buff',                container: false, construct: MechanicBuff,              premium: true },
    CANCEL:              { name: 'Cancel',              container: false, construct: MechanicCancel             },
    CHANNEL:             { name: 'Channel',             container: true,  construct: MechanicChannel            },
    CLEANSE:             { name: 'Cleanse',             container: false, construct: MechanicCleanse            },
    COMMAND:             { name: 'Command',             container: false, construct: MechanicCommand            },
    COOLDOWN:            { name: 'Cooldown',            container: false, construct: MechanicCooldown           },
    DAMAGE:              { name: 'Damage',              container: false, construct: MechanicDamage             },
    DAMAGE_BUFF:         { name: 'Damage Buff',         container: false, construct: MechanicDamageBuff         },
    DAMAGE_LORE:         { name: 'Damage Lore',         container: false, construct: MechanicDamageLore         },
    DEFENSE_BUFF:        { name: 'Defense Buff',        container: false, construct: MechanicDefenseBuff        },
    DELAY:               { name: 'Delay',               container: true,  construct: MechanicDelay              },
    DISGUISE:            { name: 'Disguise',            container: false, construct: MechanicDisguise           },
    DURABILITY:          { name: 'Durability',          container: false, construct: MechanicDurability,        premium: true },
    EXPLOSION:           { name: 'Explosion',           container: false, construct: MechanicExplosion          },
    FIRE:                { name: 'Fire',                container: false, construct: MechanicFire               },
    FLAG:                { name: 'Flag',                container: false, construct: MechanicFlag               },
    FLAG_REDUCE:         { name: 'Flag Reduce',         container: true,  construct: MechanicFlagReduce,        premium: true},
    FLAG_CLEAR:          { name: 'Flag Clear',          container: false, construct: MechanicFlagClear          },
    FLAG_TOGGLE:         { name: 'Flag Toggle',         container: false, construct: MechanicFlagToggle         },
    FOOD:                { name: 'Food',                container: false, construct: MechanicFood,              premium: true },
    FORGET_TARGETS:      { name: 'Forget Targets',      container: false, construct: MechanicForgetTargets,     premium: true },
    HEAL:                { name: 'Heal',                container: false, construct: MechanicHeal               },
    HEALTH_SET:          { name: 'Health Set',          container: false, construct: MechanicHealthSet,         premium: true },
    HEALTH:          { name: 'Health',          container: false, construct: MechanicHealth,         premium: true },
    HELD_ITEM:           { name: 'Held Item',           container: false, construct: MechanicHeldItem,          premium: true },
    IMMUNITY:            { name: 'Immunity',            container: false, construct: MechanicImmunity           },
    INTERRUPT:           { name: 'Interrupt',           container: false, construct: MechanicInterrupt          },
    ITEM:                { name: 'Item',                container: false, construct: MechanicItem               },
    ITEM_PACKET:         { name: 'Item Packet',         container: false, construct: MechanicItemPacket         },
    ITEM_PROJECTILE:     { name: 'Item Projectile',     container: true,  construct: MechanicItemProjectile     },
    ITEM_REMOVE:         { name: 'Item Remove',         container: false, construct: MechanicItemRemove         },
    LAUNCH:              { name: 'Launch',              container: false, construct: MechanicLaunch             },
    LIGHTNING:           { name: 'Lightning',           container: false, construct: MechanicLightning          },
    MANA:                { name: 'Mana',                container: false, construct: MechanicMana               },
    MESSAGE:             { name: 'Message',             container: false, construct: MechanicMessage            },
    PARTICLE:            { name: 'Particle',            container: false, construct: MechanicParticle           },
    PARTICLE_ANIMATION:  { name: 'Particle Animation',  container: false, construct: MechanicParticleAnimation  },
    PARTICLE_EFFECT:     { name: 'Particle Effect',     container: false, construct: MechanicParticleEffect,    premium: true },
    CANCEL_EFFECT:       { name: 'Cancel Effect',       container: false, construct: MechanicCancelEffect,      premium: true },
    PARTICLE_PROJECTILE: { name: 'Particle Projectile', container: true,  construct: MechanicParticleProjectile },
    ENTITY_SPREAD:       { name: 'Entity Spread',       container: true,  construct: MechanicEntitySpread },
    PASSIVE:             { name: 'Passive',             container: true,  construct: MechanicPassive            },
    PERMISSION:          { name: 'Permission',          container: false, construct: MechanicPermission         },
    POTION:              { name: 'Potion',              container: false, construct: MechanicPotion             },
    POTION_PROJECTILE:   { name: 'Potion Projectile',   container: true,  construct: MechanicPotionProjectile   },
    PROJECTILE:          { name: 'Projectile',          container: true,  construct: MechanicProjectile         },
    PURGE:               { name: 'Purge',               container: false, construct: MechanicPurge              },
    PUSH:                { name: 'Push',                container: false, construct: MechanicPush               },
    REMEMBER_TARGETS:    { name: 'Remember Targets',    container: false, construct: MechanicRememberTargets    },
    REPEAT:              { name: 'Repeat',              container: true,  construct: MechanicRepeat             },
    SOUND:               { name: 'Sound',               container: false, construct: MechanicSound              },
    SPEED:               { name: 'Speed',               container: false, construct: MechanicSpeed              },
    STATUS:              { name: 'Status',              container: false, construct: MechanicStatus             },
    TAUNT:               { name: 'Taunt',               container: false, construct: MechanicTaunt              },
    TRIGGER:             { name: 'Trigger',             container: true,  construct: MechanicTrigger,           premium: true },
    DATA_EDIT:           { name: 'Data Edit',           container: false, construct: MechanicDataEdit           },
    VALUE_ADD:           { name: 'Value Add',           container: false, construct: MechanicValueAdd           },
    VALUE_ATTRIBUTE:     { name: 'Value Attribute',     container: false, construct: MechanicValueAttribute     },
    VALUE_COPY:          { name: 'Value Copy',          container: false, construct: MechanicValueCopy,         premium: true },
    VALUE_DISTANCE:      { name: 'Value Distance',      container: false, construct: MechanicValueDistance,     premium: true },
    VALUE_HEALTH:        { name: 'Value Health',        container: false, construct: MechanicValueHealth,       premium: true },
    VALUE_LOCATION:      { name: 'Value Location',      container: false, construct: MechanicValueLocation      },
    VALUE_LORE:          { name: 'Value Lore',          container: false, construct: MechanicValueLore          },
    VALUE_LORE_SLOT:     { name: 'Value Lore Slot',     container: false, construct: MechanicValueLoreSlot,     premium: true},
    VALUE_MANA:          { name: 'Value Mana',          container: false, construct: MechanicValueMana,         premium: true },
    VALUE_MULTIPLY:      { name: 'Value Multiply',      container: false, construct: MechanicValueMultiply      },
    VALUE_PLACEHOLDER:   { name: 'Value Placeholder',   container: false, construct: MechanicValuePlaceholder,  premium: true },
    //VALUE_MMOITEMS_ATTR:   { name: 'Value MMoItem Attr',   container: false, construct: MechanicValueMMoItemAttr,  premium: true },
    VALUE_NEONFLASH_ATTR:   { name: 'Value NeonFlash Attr',   container: false, construct: MechanicValueNeonFlashAttr,  premium: true },
    VALUE_RANDOM:        { name: 'Value Random',        container: false, construct: MechanicValueRandom        },
    VALUE_SET:           { name: 'Value Set',           container: false, construct: MechanicValueSet           },
    WARP:                { name: 'Warp',                container: false, construct: MechanicWarp               },
    WARP_LOC:            { name: 'Warp Location',       container: false, construct: MechanicWarpLoc            },
    WARP_RANDOM:         { name: 'Warp Random',         container: false, construct: MechanicWarpRandom         },
    WARP_SWAP:           { name: 'Warp Swap',           container: false, construct: MechanicWarpSwap           },
    WARP_TARGET:         { name: 'Warp Target',         container: false, construct: MechanicWarpTarget         },
    WARP_VALUE:          { name: 'Warp Value',          container: false, construct: MechanicWarpValue          },
    WOLF:                { name: 'Wolf',                container: true,  construct: MechanicWolf               },
    ARMOR_STAND:         { name: 'Armor Stand',         container: true,  construct: MechanicArmorStand         },
    MYTHIC_FACTION:      { name: 'Mythic Faction',      container: true,  construct: MechanicMythicFaction,      premium: true },
    MYTHIC_SUMMON:       { name: 'Mythic Summon',       container: true,  construct: MechanicMythicSummon,      premium: true },
    Adyeshach:           { name: 'Adyeshach',           container: true,  construct: MechanicAdyeshach          },
    ARMOR_STAND_REMOVE:  { name: 'Armor Stand Remove',  container: true,  construct: MechanicArmorStandRemove   },
    GERM_ANIMATION_START:{ name: 'Germ Animation Start',container: true,  construct: MechanicGermAnimationStart },
    GERM_ANIMATION_STOP: { name: 'Germ Animation Stop', container: true,  construct: MechanicGermAnimationStop  },
    RETURN:              { name: 'Return',              container: true,  construct: MechanicReturn             },
    MYTHIC_CAST:         { name: 'Mythic cast',         container: false, construct: MechanicMythicCast         },
    MYTHIC_CAST_TARGET:  { name: 'Mythic cast target',  container: false, construct: MechanicMythicCastTarget   },
    ARENA_SHOW:          { name: 'Arena Show',          container: false, construct: MechanicArenaShow          },
    SKILL_CAST:          { name: 'skill cast',          container: true,  construct: MechanicSkillCast,         premium: true },
    // mythic summon delete
    MYTHIC_SUMMON_DELETE:{ name: 'Mythic Summon Delete',container: true,  construct: MechanicMythicSummonDelete,premium: true },
    MYTHIC_SUMMON_VALUE:{ name: 'Mythic Summon Value',container: true,  construct: MechanicMythicSummonValue,premium: true },
    BADGE_COOLDOWN_RESET:{ name: 'Badge Cooldown Reset',container: true,  construct: MechanicBadgeCooldownReset,premium: true }, // 2025/11/30
    BADGE_COOLDOWN_SCALE:{ name: 'Badge Cooldown Scale',container: true,  construct: MechanicBadgeCooldownScale,premium: true }, // 2025/12/25

    TRAIL_START:{ name: 'Trail Start',container: true,  construct: MechanicTrailStart,premium: true }, // 2026/01/19
    TRAIL_END:{ name: 'Trail End',container: true,  construct: MechanicTrailEnd,premium: true }, // 2026/01/19
    Runic_REDUCE:{ name: 'Runic Reduce',container: true,  construct: MechanicRunicReduce,premium: true},
    REVIVE:{ name: 'Revive',container: true,  construct: MechanicRevive,premium: true},
    NEONFLASH_ATTRIBUTE:{ name: 'Neonflash Attribute',container: true,  construct: MechanicNeonflashAttribute,premium: true},  // 2026/4/4
    GRAVITY_FIELD:{ name: 'Gravity Field',container: true,  construct: MechanicGravityField,premium: true},  // 2026/4/4

    BUFF_HUD:{ name: 'Buff Hud',container: true,  construct: MechanicBuffHud,premium: true},  // 2026/11/9
    GERM_DOS:{ name: 'Germ Dos',container: true,  construct: MechanicGermDos,premium: true},  // 2026/11/9
    CRACK:{ name: 'Crack',container: true,  construct: MechanicCrak,premium: true},  // 2026/11/9
    INTERRUPT_PARRY:{ name: 'Interrupt Parry',container: true,  construct: MechanicInterruptParry,premium: true}  // 2026/11/13
    // MechanicRunicReduce
};

var saveIndex;

/**
 * Represents a component of a dynamic skill
 *
 * @param {string}    name      - name of the component
 * @param {string}    type      - type of the component
 * @param {boolean}   container - whether or not the component can contain others
 * @param {Component} [parent]  - parent of the component if any
 *
 * @constructor
 */
function Component(name, type, container, parent) {
    this.name = name;
    this.type = type;
    this.container = container;
    this.parent = parent;
    this.html = undefined;
    this.components = [];
    this.data = [
        new StringValue('图标键', 'icon-key', '')
            .setTooltip('图标 Lore 中使用的键。如设为 example 且值名为 value，可通过字符串 {attr:example.value} 引用。'),
        new ListValue('全局上下文', 'use-global-context', [ 'True', 'False' ], 'False')
            .setTooltip('用于确认该组件是否使用全局上下文用于数据交互, 默认使用技能体私有上下文。')
    ];
    if (this.type === Type.MECHANIC) {
        this.data.push(new ListValue('计为施法', 'counts', [ 'True', 'False' ], 'True')
            .setTooltip('该组件的执行是否视为「已施法」，从而消耗法力并开始冷却。若这是技能失败时（清理、错误提示等）执行的组件，请设为 false。')
        );
    }
    else if (this.type === Type.TRIGGER && name != 'Cast' && name != 'Initialize' && name != 'Cleanup')
    {
        this.data.push(new ListValue('法力', 'mana', [ 'True', 'False' ], 'False')
            .setTooltip('该触发器是否要求消耗法力才能激活')
        );
        this.data.push(new ListValue('冷却', 'cooldown', [ 'True', 'False' ], 'False')
            .setTooltip('该触发器是否要求冷却结束后才能激活')
        );
    }

    this.dataKey = 'data';
    this.componentKey = 'children';
}

Component.prototype.dupe = function(parent) {
    var i;
    var ele = new Component(this.name, this.type, this.container, parent);
    for (i = 0; i < this.components.length; i++) {
        ele.components.push(this.components[i].dupe(ele));
    }
    ele.data = ele.data.slice(0, 1);
    for (i = ele.data.length; i < this.data.length; i++) {
        ele.data.push(copyRequirements(this.data[i], this.data[i].dupe()));
    }
    ele.description = this.description;
    return ele;
}

/**
 * Creates the builder HTML element for the component and
 * appends it onto the target HTML element.
 *
 * @param {Element} target - the HTML element to append the result to
 */
Component.prototype.createBuilderHTML = function(target)
{
    // Create the wrapping divs with the appropriate classes
    var container = document.createElement('div');
    container.comp = this;
    if (this.type === Type.TRIGGER) {
        container.className = 'componentWrapper';
    }

    var div = document.createElement('div');
    div.className = 'component ' + this.type;
    if (this.type !== Type.TRIGGER) {
        div.draggable = true;
        div.ondragstart = this.drag;
    }
    div.ondrop = this.drop;
    if (this.container) {
        div.ondragover = this.allowDrop;
    }

    // 拖拽手柄（仅提示可拖动，实际拖动仍由整张卡片承担）
    if (this.type !== Type.TRIGGER) {
        var grip = document.createElement('span');
        grip.className = 'dragGrip';
        grip.textContent = '⋮⋮';
        grip.title = '拖动调整位置';
        div.appendChild(grip);
    }

    // Component label
    var label = document.createElement('h3');
    label.title = '编辑 ' + this.name + ' 选项';
    label.className = this.type + 'Label';
    label.innerHTML = this.name;
    label.component = this;
    label.addEventListener('click', function(e) {
        var current = document.querySelector('.component.selected');
        if (current) {
            current.classList.remove('selected');
        }
        this.parentNode.classList.add('selected');
        this.component.createFormHTML();
        showSkillPage('skillForm');
    });
    div.appendChild(label);

    // Container components can add children so they get a button
    if (this.container)
    {
        var add = document.createElement('div');
        add.className = 'builderButton';
        add.innerHTML = '+ 添加组件';
        add.component = this;
        add.addEventListener('click', function(e) {
            activeComponent = this.component;
            showSkillPage('componentChooser');
        });
        div.appendChild(add);

        var vision = document.createElement('div');
        vision.title = '隐藏组件';
        vision.className = 'builderButton smallButton';
        vision.style.background = 'url("editor/img/eye.png") no-repeat center #222';
        vision.component = this;
        vision.addEventListener('click', function(e) {
            var comp = this.component;
            if (comp.childrenHidden) {
                comp.childDiv.style.display = 'block';
                this.style.backgroundImage = 'url("editor/img/eye.png")';
            } else {
                comp.childDiv.style.display = 'none';
                this.style.backgroundImage = 'url("editor/img/eyeShaded.png")';
            }
            comp.childrenHidden = !comp.childrenHidden;
        });
        div.appendChild(vision);
        this.childrenHidden = false;
    }

    // Add the duplicate button
    if (this.type !== Type.TRIGGER) {
        var duplicate = document.createElement('div');
        duplicate.className = 'builderButton smallButton';
        duplicate.title = '复制';
        duplicate.style.background = 'url("editor/img/duplicate.png") no-repeat center #222';
        duplicate.component = this;
        duplicate.addEventListener('click', function(e) {
            var comp = this.component;
            var copy = comp.dupe(comp.parent);
            comp.parent.components.push(copy);
            copy.createBuilderHTML(comp.parent.html);
            if (typeof toast === 'function') {
                toast('已复制组件：' + comp.name);
            }
        });
        div.appendChild(duplicate);
    }

    // Add the remove button
    var remove = document.createElement('div');
    remove.title = '删除';
    remove.className = 'builderButton smallButton cancelButton';
    remove.style.background = 'url("editor/img/delete.png") no-repeat center #f00';
    remove.component = this;
    remove.addEventListener('click', function(e) {
        var comp = this.component;
        var wrapper = this.parentNode.parentNode;
        var execute = function() {
            var list = comp.parent.components;
            for (var i = 0; i < list.length; i++) {
                if (list[i] === comp) {
                    list.splice(i, 1);
                    break;
                }
            }
            if (wrapper.parentNode) {
                wrapper.parentNode.removeChild(wrapper);
            }
            showSkillPage('builder');
            if (typeof toast === 'function') {
                toast('已删除组件：' + comp.name, 'warn');
            }
        };
        if (typeof showConfirm === 'function') {
            showConfirm('确定删除组件「' + comp.name + '」及其所有子组件吗？', execute);
        }
        else {
            execute();
        }
    });
    div.appendChild(remove);

    container.appendChild(div);

    // Apply child components
    var childContainer = document.createElement('div');
    childContainer.className = 'componentChildren';
    if (this.components.length > 0) {
        for (var i = 0; i < this.components.length; i++)
        {
            this.components[i].createBuilderHTML(childContainer);
        }
    }
    container.appendChild(childContainer);
    this.childDiv = childContainer;

    // Append the content
    target.appendChild(container);

    this.html = childContainer;

    // 组件树重建时清空搜索，避免新组件被过滤规则隐藏
    var treeSearch = document.getElementById('treeSearch');
    if (treeSearch && treeSearch.value) {
        treeSearch.value = '';
        if (typeof filterTree === 'function') {
            filterTree('');
        }
    }
};

Component.prototype.allowDrop = function(e) {
    e.preventDefault();
    if (hoverSpace) {
        hoverSpace.classList.remove('dropTarget');
        hoverSpace.onmouseout = undefined;
    }
    hoverSpace = e.target;
    while (hoverSpace.className.indexOf('component') < 0) {
        hoverSpace = hoverSpace.parentNode;
    }
    var thing = document.getElementById('dragComponent');
    if (hoverSpace.id != 'dragComponent' && hoverSpace.parentNode.comp.container && canDrop(thing, hoverSpace)) {
        hoverSpace.classList.add('dropTarget');
        hoverSpace.onmouseout = function() {
            if (!hoverSpace) {
                this.onmouseout = undefined;
                return;
            }
            hoverSpace.classList.remove('dropTarget');
            hoverSpace.onmouseout = undefined;
            hoverSpace = undefined;
        }
    }
    else hoverSpace = undefined;
};

Component.prototype.drag = function(e) {
    e.dataTransfer.setData('text', 'anything');
    var dragged = document.getElementById('dragComponent');
    if (dragged) {
        dragged.id = '';
    }
    e.target.id = 'dragComponent';
};

Component.prototype.drop = function(e) {
    if (hoverSpace) {
        hoverSpace.classList.remove('dropTarget');
        hoverSpace = undefined;
    }

    e.preventDefault();
    var thing = document.getElementById('dragComponent').parentNode;
    var target = e.target;
    while (target.className.indexOf('component') < 0) {
        target = target.parentNode;
    }
    if (target.id == 'dragComponent' || !target.parentNode.comp.container || !canDrop(thing, target)) {
        return;
    }
    var targetComp = target.parentNode.comp;
    var thingComp = thing.comp;
    target = target.parentNode.childNodes[1];
    thing.parentNode.removeChild(thing);
    target.appendChild(thing);

    thingComp.parent.components.splice(thingComp.parent.components.indexOf(thingComp), 1);
    thingComp.parent = targetComp;
    thingComp.parent.components.push(thingComp);
};

/**
 * Creates the form HTML for editing the component data and
 * applies it to the appropriate part of the page.
 */
Component.prototype.createFormHTML = function()
{
    var target = document.getElementById('skillForm');

    var form = document.createElement('form');

    var header = document.createElement('h4');
    header.innerHTML = this.name;
    form.appendChild(header);

    if (this.description)
    {
        var desc = document.createElement('p');
        desc.innerHTML = this.description;
        form.appendChild(desc);
    }

    if (this.data.length > 1)
    {
        var h = document.createElement('hr');
        form.appendChild(h);

        var i = 1;
        for (var j = 1; j < this.data.length; j++) {
            if (this.data[j] instanceof AttributeValue) {
                i = 0;
                break;
            }
        }
        for (; i < this.data.length; i++)
        {
            this.data[i].hidden = false;
            this.data[i].createHTML(form);
        }
    }

    var hr = document.createElement('hr');
    form.appendChild(hr);

    var done = document.createElement('h5');
    done.className = 'doneButton';
    done.innerHTML = '确认';
    done.component = this;
    done.addEventListener('click', function(e) {
        this.component.update();
        document.getElementById('skillForm').removeChild(this.component.form);
        showSkillPage('builder');
    });
    form.appendChild(done);

    this.form = form;

    target.innerHTML = '';
    target.appendChild(form);
    activeComponent = this;

    for (var i = 0; i < this.data.length; i++)
    {
        this.data[i].applyRequireValues();
    }
}

/**
 * Updates the component using the form data if it exists
 */
Component.prototype.update = function()
{
    for (var j = 0; j < this.data.length; j++)
    {
        this.data[j].update();
    }
}

/**
 * Gets the save string for the component
 *
 * @param {string} spacing - spacing to put before the data
 */
Component.prototype.getSaveString = function(spacing)
{
    this.createFormHTML();

    var id = '';
    var index = saveIndex;
    while (index > 0 || id.length == 0)
    {
        id += String.fromCharCode((index % 26) + 97);
        index = Math.floor(index / 26);
    }
    var result = spacing + this.name + '-' + id + ":\n";
    saveIndex++;

    result += spacing + "  type: '" + this.type + "'\n";
    if (this.data.length > 0)
    {
        result += spacing + '  data:\n';
        for (var i = 0; i < this.data.length; i++)
        {
            if (!this.data[i].hidden)
                result += this.data[i].getSaveString(spacing + '    ');
        }
    }
    if (this.components.length > 0)
    {
        result += spacing + '  children:\n';
        for (var j = 0; j < this.components.length; j++)
        {
            result += this.components[j].getSaveString(spacing + '    ');
        }
    }
    return result;
}

/**
 * Loads component data from the config lines stating at the given index
 *
 * @param {YAMLObject} data - the data to load
 *
 * @returns {Number} the index of the last line of data for this component
 */
Component.prototype.load = loadSection;

// -- Trigger constructors ----------------------------------------------------- //

extend('TriggerBlockBreak', 'Component');
function TriggerBlockBreak() {
    this.super('Block Break', Type.TRIGGER, true);
    this.description = '玩家破坏与设定匹配的方块时执行技能效果';

    this.data.push(new MultiListValue('材质', 'material', [ 'Any' ].concat(materialList), [ 'Any' ])
        .setTooltip('需要被破坏的方块类型')
    );
    this.data.push(new IntValue('数据', 'data', -1)
        .setTooltip('方块的数据值（-1 表示任意数据值）')
    );
}

extend('TriggerBlockPlace', 'Component');
function TriggerBlockPlace() {
    this.super('Block Place', Type.TRIGGER, true);
    this.description = '玩家放置与设定匹配的方块时执行技能效果';

    this.data.push(new MultiListValue('材质', 'material', [ 'Any' ].concat(materialList), [ 'Any' ])
        .setTooltip('需要被放置的方块类型')
    );
    this.data.push(new IntValue('数据', 'data', -1)
        .setTooltip('方块的数据值（-1 表示任意数据值）')
    );
}

extend('TriggerCast', 'Component');
function TriggerCast()
{
    this.super('Cast', Type.TRIGGER, true);

    this.description = '玩家通过施法指令、技能栏或点击组合施放技能时执行技能效果。';
}

extend('TriggerCleanup', 'Component');
function TriggerCleanup()
{
    this.super('Cleanup', Type.TRIGGER, true);

    this.description = '玩家断线或遗忘该技能时执行技能效果。为便于计算，此处始终按技能等级 1 执行。';
}

extend('TriggerCrouch', 'Component');
function TriggerCrouch()
{
    this.super('Crouch', Type.TRIGGER, true);

    this.description = '玩家按下 Shift 开始或结束潜行时执行技能效果。';

    this.data.push(new ListValue('类型', 'type', [ 'Start Crouching', 'Stop Crouching', 'Both' ], 'Start Crouching')
        .setTooltip('是否只在潜行（或非潜行）时执行子组件')
    );
}

extend('TriggerDeath', 'Component');
function TriggerDeath()
{
    this.super('Death', Type.TRIGGER, true);

    this.description = '玩家死亡时执行技能效果。';

}

extend('TriggerEnvironmentDamage', 'Component');
function TriggerEnvironmentDamage()
{
    this.super('Environment Damage', Type.TRIGGER, true);

    this.description = '玩家受到环境伤害时执行技能效果。';

    this.data.push(new ListValue('类型', 'type', DAMAGE_TYPES, 'FALL')
        .setTooltip('要响应的伤害来源')
    );
}


extend('TriggerInitialize', 'Component');
function TriggerInitialize()
{
    this.super('Initialize', Type.TRIGGER, true);

    this.description = '立即执行技能效果，可用于被动能力。';
}

extend('TriggerKill', 'Component');
function TriggerKill()
{
    this.super('Kill', Type.TRIGGER, true);

    this.description = '击杀目标时执行技能效果';

    this.data.push(new ListValue('类型', 'type', ['player', 'monster', 'all'], 'all')
        .setTooltip('触发生物种类')
    );
}

extend('TriggerLand', 'Component');
function TriggerLand()
{
    this.super('Land', Type.TRIGGER, true);

    this.description = '玩家落回地面时执行技能效果。';

    this.data.push(new DoubleValue('最小距离', 'min-distance', 0)
        .setTooltip('触发前玩家需要下落的最小距离。')
    );
}

extend('TriggerLaunch', 'Component');
function TriggerLaunch()
{
    this.super('Launch', Type.TRIGGER, true);

    this.description = '玩家发射抛射物时执行技能效果。';

    this.data.push(new ListValue('类型', 'type', [ 'Any', 'Arrow', 'Egg', 'Ender Pearl', 'Fireball', 'Fishing Hook', 'Snowball' ], 'Any')
        .setTooltip('需要发射的抛射物类型。')
    );
}

extend('TriggerBounceCollision', 'Component');
function TriggerBounceCollision()
{
    this.super('Bounce Collision', Type.TRIGGER, true);

    this.description = '反弹抛射物（Bounce Projectile）碰撞方块或生物时触发。可用值：api-bounce-type（BLOCK/ENTITY）、api-bounce-location、api-bounce-generation、api-bounce-block、api-bounce-target、api-bounce-normal-x/y/z。';

    this.data.push(new ListValue('碰撞类型', 'type', [ 'Any', 'Block', 'Entity' ], 'Any')
        .setTooltip('只响应对应类型的碰撞：Any 全部，Block 只响应撞方块，Entity 只响应命中生物。')
    );
    this.data.push(new IntValue('代数', 'generation', -1)
        .setTooltip('只响应指定代数的抛射物，-1 表示所有代数；0 为初始母体。')
    );
}

extend('TriggerBounceSplit', 'Component');
function TriggerBounceSplit()
{
    this.super('Bounce Split', Type.TRIGGER, true);

    this.description = '反弹抛射物分裂出子体后触发。可用值：api-bounce-generation、api-bounce-count、api-bounce-location。';

    this.data.push(new IntValue('代数', 'generation', -1)
        .setTooltip('只响应指定代数的子体分裂，-1 表示所有代数。')
    );
}

extend('TriggerBounceEnd', 'Component');
function TriggerBounceEnd()
{
    this.super('Bounce End', Type.TRIGGER, true);

    this.description = '反弹抛射物结束时触发一次（限制、过期、取消等）。可用值：api-bounce-reason、api-bounce-generation、api-bounce-location。';

    this.data.push(new ListValue('原因', 'reason', [ 'Any', 'BOUNCE_LIMIT', 'UNSUPPORTED_BLOCK', 'NO_REFLECTION', 'SPLIT', 'EXPIRED', 'UNLOADED_CHUNK', 'CANCELLED' ], 'Any')
        .setTooltip('只响应指定的结束原因：Any 全部，其余为具体原因。')
    );
}

extend('TriggerMove', 'Component');
function TriggerMove()
{
    this.super('Move', Type.TRIGGER, true);

    this.description = '玩家移动时执行技能效果。移动期间每个 tick 都会触发，请谨慎使用；可用 api-moved 值获取移动距离。';
}

extend('TriggerPhysicalDamage', 'Component');
function TriggerPhysicalDamage()
{
    this.super('Physical Damage', Type.TRIGGER, true);

    this.description = '玩家造成物理（非技能）伤害时执行技能效果，包括近战攻击和弓箭射击。';

    this.data.push(new ListValue('目标施法者', 'target', [ 'True', 'False' ], 'True')
        .setTooltip('True 时子组件以施法者为目标，False 时以被伤害的实体为目标')
    );
    this.data.push(new ListValue('类型', 'type', [ 'Both', 'Melee', 'Projectile' ], 'Both')
        .setTooltip('造成的伤害类型')
    );
    this.data.push(new DoubleValue("最小伤害", "dmg-min", 0)
        .setTooltip('需要造成的最小伤害')
    );
    this.data.push(new DoubleValue("最大伤害", "dmg-max", 999)
        .setTooltip('需要造成的最大伤害')
    );
}

extend('TriggerSkillDamage', 'Component');
function TriggerSkillDamage()
{
    this.super('Skill Damage', Type.TRIGGER, true);

    this.description = '玩家用技能造成伤害时执行技能效果。';

    this.data.push(new ListValue('目标施法者', 'target', [ 'True', 'False' ], 'True')
        .setTooltip('True 时子组件以施法者为目标，False 时以被伤害的实体为目标')
    );
    this.data.push(new DoubleValue("最小伤害", "dmg-min", 0)
        .setTooltip('需要造成的最小伤害')
    );
    this.data.push(new DoubleValue("最大伤害", "dmg-max", 999)
        .setTooltip('需要造成的最大伤害')
    );
    this.data.push(new StringListValue('分类', 'category', [ 'default' ] )
        .setTooltip('要响应的技能伤害类型，留空表示所有技能伤害。')
    );
}

extend('TriggerTookPhysicalDamage', 'Component');
function TriggerTookPhysicalDamage()
{
    this.super('Took Physical Damage', Type.TRIGGER, true);

    this.description = '玩家受到物理（非技能）伤害时执行技能效果，包括近战攻击和非技能抛射物。';

    this.data.push(new ListValue('目标施法者', 'target', [ 'True', 'False' ], 'True')
        .setTooltip('True 时子组件以施法者为目标，False 时以攻击者为目标')
    );
    this.data.push(new ListValue('类型', 'type', [ 'Both', 'Melee', 'Projectile' ], 'Both')
        .setTooltip('造成的伤害类型')
    );
    this.data.push(new DoubleValue("最小伤害", "dmg-min", 0)
        .setTooltip('需要造成的最小伤害')
    );
    this.data.push(new DoubleValue("最大伤害", "dmg-max", 999)
        .setTooltip('需要造成的最大伤害')
    );
}

extend('TriggerTookSkillDamage', 'Component');
function TriggerTookSkillDamage()
{
    this.super('Took Skill Damage', Type.TRIGGER, true);

    this.description = '玩家受到来自他人技能的伤害时执行技能效果。';

    this.data.push(new ListValue('目标施法者', 'target', [ 'True', 'False' ], 'True')
        .setTooltip('True 时子组件以施法者为目标，False 时以攻击者为目标')
    );
    this.data.push(new ListValue('伤害类型', 'damageType', [ 'PLAYER', 'ENTITY', 'ALL' ], 'ALL')
        .setTooltip('筛选攻击者生物种类')
    );
    this.data.push(new ListValue('目标类型', 'targetType', [ 'PLAYER', 'ENTITY', 'ALL' ], 'ALL')
        .setTooltip('筛选目标者生物种类')
    );
    this.data.push(new DoubleValue("最小伤害", "dmg-min", 0)
        .setTooltip('需要造成的最小伤害')
    );
    this.data.push(new DoubleValue("最大伤害", "dmg-max", 999)
        .setTooltip('需要造成的最大伤害')
    );
    this.data.push(new StringListValue('分类', 'category', [ 'default' ] )
        .setTooltip('要响应的技能伤害类型，留空表示所有技能伤害。')
    );
}

// TriggerMythicSummonDeath
extend('TriggerMythicSummonDeath', 'Component');
function TriggerMythicSummonDeath()
{
    this.super('Mythic Summon Death', Type.TRIGGER, true);

    this.description = '当召唤物死亡、清除时触发，可判断种类';

    this.data.push(new StringListValue('神话类型', 'mythicType', [ 'default' ] )
        .setTooltip('种类列表，MM 生物配置的 ID 非怪物展示名称')
    );
}
// TriggerPlayerTeamStart
extend('TriggerPlayerTeamStart', 'Component');
function TriggerPlayerTeamStart()
{
    this.super('Player Team Start', Type.TRIGGER, true);

    this.description = '副本其它完成后对玩家触发';

    this.data.push(new StringListValue('副本', 'dungeon', [ '' ] )
        .setTooltip('筛选副本名称')
    );
    this.data.push(new StringListValue('难度', 'difficulty', [ '' ] )
        .setTooltip('筛选副本难度')
    );
}

extend('TriggerPlayerArenaStart', 'Component');
function TriggerPlayerArenaStart()
{
    this.super('Player Arena Start', Type.TRIGGER, true);

    this.description = '对战其它完成后对玩家触发';
}

extend('TriggerPlayerKillerEntity', 'Component');
function TriggerPlayerKillerEntity()
{
    this.super('Player Killer Entity', Type.TRIGGER, true);

    this.description = '玩家使用技能击杀敌人时触发';

    this.data.push(new StringListValue('技能', 'skill', [ '' ] )
        .setTooltip('击杀时的技能')
    );
    this.data.push(new StringListValue('分类', 'classification', [ '' ] )
        .setTooltip('伤害类型')
    );
    this.data.push(new ListValue("身后", "behind", ['True', 'False'], 'False')
        .setTooltip('是否要求击杀者处于目标背部')
    );
}

extend('TriggerPlayerHealthRestore', 'Component');
function TriggerPlayerHealthRestore()
{
    this.super('Player Health Restore', Type.TRIGGER, true);

    this.description = '玩家恢复生命值时触发， 可通过Key`PLAYER_HEALTH_RESTORE`取得恢复量';

    this.data.push(new IntValue('最小数量', 'minAmount', 1)
        .setTooltip('要求的最小存在数量')
    );
    this.data.push(new IntValue('最大数量', 'maxAmount', 999)
        .setTooltip('要求的最大存在数量')
    );
}

// TriggerSkillLink
extend('TriggerSkillLink', 'Component');
function TriggerSkillLink()
{
    this.super('Skill Link', Type.TRIGGER, true);

    this.description = '技能连续触发器。两次同名技能衔接间隔1秒';

    this.data.push(new IntValue('数量', 'amount', 2)
        .setTooltip('匹配衔接次数')
    );
    this.data.push(new StringListValue('技能', 'skill', [ '' ] )
        .setTooltip('匹配技能名称')
    );
    this.data.push(new StringListValue('分类', 'classification', [ '' ] )
        .setTooltip('匹配伤害类型')
    );
}

// TriggerTrailHurt
extend('TriggerTrailHurt', 'Component');
function TriggerTrailHurt()
{
    this.super('Trail Hurt', Type.TRIGGER, true);

    this.description = '路径特效追踪';

    this.data.push(new StringValue('特效名称', 'effectName', '' )
        .setTooltip('匹配触发的特效名称')
    );

}

// TriggerCombatEnter
extend('TriggerCombatEnter', 'Component');
function TriggerCombatEnter()
{
    this.super('Combat Enter', Type.TRIGGER, true);

    this.description = '参战状态开始';

}

extend('TriggerCombatLeave', 'Component');
function TriggerCombatLeave()
{
    this.super('Combat Leave', Type.TRIGGER, true);

    this.description = '参战状态结束';

}

extend('TriggerMagicPulse', 'Component');
function TriggerMagicPulse()
{
    this.super('Magic Pulse', Type.TRIGGER, true);

    this.description = '玩家使用魔法豆时';

    this.data.push(new IntValue('最小数量', 'minAmount', 1)
        .setTooltip('要求的最小数量')
    );

    this.data.push(new IntValue('最大数量', 'maxAmount', 999)
        .setTooltip('要求的最大数量')
    );
}
// TriggerAttributeChange
extend('TriggerAttributeChange', 'Component');
function TriggerAttributeChange()
{
    this.super('Attribute Change', Type.TRIGGER, true);

    this.description = '属性修改切换触发';

    this.data.push(new StringListValue('容器', 'container', [ '' ] )
        .setTooltip('匹配触发属性修改的容器，用于排除非不要的属性切换通知')
    );
    this.data.push(new StringListValue('属性列表', 'attributes', [ '' ] )
        .setTooltip('筛选属性名称、类型')
    );
}

extend('TriggerKnee', 'Component');
function TriggerKnee()
{
    this.super('Knee', Type.TRIGGER, true);

    this.description = '副本倒地触发器';

}
// TriggerEndKnee
extend('TriggerEndKnee', 'Component');
function TriggerEndKnee()
{
    this.super('Knee End', Type.TRIGGER, true);

    this.description = '副本倒地结束触发器';

}
// TriggerPlayerUseConsume
extend('TriggerPlayerUseConsume', 'Component');
function TriggerPlayerUseConsume()
{
    this.super('Use Consume', Type.TRIGGER, true);

    this.description = '玩家使用消耗品时触发';

}


// -- Target constructors ------------------------------------------------------ //

extend('TargetArea', 'Component');
function TargetArea()
{
    this.super('Area', Type.TARGET, true);

    this.description = '以当前目标（默认是施法玩家）为圆心，选中半径内所有单位。';

    this.data.push(new AttributeValue("半径", "radius", 3, 0)
        .setTooltip('目标区域的半径（格）')
    );
    this.data.push(new ListValue("分组", "group", ["Ally", "Enemy", "Both"], "Enemy")
        .setTooltip('目标筛选方式')
    );
    this.data.push(new ListValue("穿墙", "wall", ['True', 'False'], 'False')
        .setTooltip('是否允许目标位于墙的另一侧')
    );
    this.data.push(new ListValue("包含施法者", "caster", [ 'True', 'False' ], 'False')
        .setTooltip('是否把施法者包含进目标列表')
    );
    this.data.push(new AttributeValue("最大目标数", "max", 99, 0)
        .setTooltip('最多对多少个目标执行子组件')
    );
}

extend('TargetCone', 'Component');
function TargetCone()
{
    this.super('Cone', Type.TARGET, true);

    this.description = '以当前目标（默认是施法玩家）为起点，选中前方直线上的所有单位。若包含施法者，也计入最大数量。';

    this.data.push(new AttributeValue("范围", "range", 5, 0)
        .setTooltip('目标可处于的最大距离（格）')
    );
    this.data.push(new AttributeValue("角度", "angle", 90, 0)
        .setTooltip('锥形扇面的角度（度）')
    );
    this.data.push(new ListValue("分组", "group", ["Ally", "Enemy", "Both"], "Enemy")
        .setTooltip('目标筛选方式')
    );
    this.data.push(new ListValue("穿墙", "wall", ['True', 'False'], 'False')
        .setTooltip('是否允许目标位于墙的另一侧')
    );
    this.data.push(new ListValue("包含施法者", "caster", [ 'True', 'False' ], 'False')
        .setTooltip('是否把施法者包含进目标列表')
    );
    this.data.push(new AttributeValue("最大目标数", "max", 99, 0)
        .setTooltip('最多对多少个目标执行子组件')
    );
}

extend('TargetLinear', 'Component');
function TargetLinear()
{
    this.super('Linear', Type.TARGET, true);

    this.description = '以当前目标（默认是施法玩家）为起点，选中前方直线上的所有单位。';

    this.data.push(new AttributeValue("范围", "range", 5, 0)
        .setTooltip('目标可处于的最大距离（格）')
    );
    this.data.push(new AttributeValue("容差", "tolerance", 4, 0)
        .setTooltip('瞄准的宽松程度。数值越大越容易命中，本质上是判定锥形的宽度。')
    );
    this.data.push(new ListValue("分组", "group", ["Ally", "Enemy", "Both"], "Enemy")
        .setTooltip('目标筛选方式')
    );
    this.data.push(new ListValue("穿墙", "wall", ['True', 'False'], 'False')
        .setTooltip('是否允许目标位于墙的另一侧')
    );
    this.data.push(new ListValue("包含施法者", "caster", [ 'True', 'False' ], 'False')
        .setTooltip('是否把施法者包含进目标列表')
    );
    this.data.push(new AttributeValue("最大目标数", "max", 99, 0)
        .setTooltip('最多对多少个目标执行子组件')
    );
}

extend('TargetLocation', 'Component');
function TargetLocation()
{
    this.super('Location', Type.TARGET, true);

    this.description = '以目标或施法者准星所指位置为目标，可与其它目标类型组合实现远程范围效果。';

    this.data.push(new AttributeValue('范围', 'range', 5, 0)
        .setTooltip('该位置的最大距离')
    );
    this.data.push(new ListValue('仅地面', 'ground', [ 'True', 'False' ], 'True')
        .setTooltip('是否只允许玩家瞄准地面或其它单位')
    );
}

// TargetRandomLocation
extend('TargetRandomLocation', 'Component');
function TargetRandomLocation()
{
    this.super('Random Location', Type.TARGET, true);

    this.description = '随机位置目标';

    this.data.push(new AttributeValue('最小范围', 'minRange', 1.0, 0)
        .setTooltip('最小范围')
    );
    this.data.push(new AttributeValue('最大范围', 'maxRange', 5.0, 0)
        .setTooltip('最大范围')
    );

    this.data.push(new IntValue('最小数量', 'minAmount', 1)
        .setTooltip('最小数量')
    );
    this.data.push(new IntValue('最大数量', 'maxAmount', 1)
        .setTooltip('最大数量')
    );

    this.data.push(new AttributeValue('最小距离', 'minDistance', 0.0, 0)
        .setTooltip('最小距离')
    );
    this.data.push(new AttributeValue('最大距离', 'maxDistance', 5.0, 0)
        .setTooltip('最大距离')
    );

    this.data.push(new ListValue('仅地面', 'ground', [ 'True', 'False' ], 'True')
        .setTooltip('是否只允许玩家瞄准地面或其它单位')
    );
}


extend('TargetNearest', 'Component');
function TargetNearest()
{
    this.super('Nearest', Type.TARGET, true);

    this.description = '以当前目标（默认是施法玩家）为圆心，选中半径内最近的单位。若包含施法者，也计入最大数量。';

    this.data.push(new AttributeValue("半径", "radius", 3, 0)
        .setTooltip('目标区域的半径（格）')
    );
    this.data.push(new ListValue("分组", "group", ["Ally", "Enemy", "Both"], "Enemy")
        .setTooltip('目标筛选方式')
    );
    this.data.push(new ListValue("穿墙", "wall", ['True', 'False'], 'False')
        .setTooltip('是否允许目标位于墙的另一侧')
    );
    this.data.push(new ListValue("包含施法者", "caster", [ 'True', 'False' ], 'False')
        .setTooltip('是否把施法者包含进目标列表')
    );
    this.data.push(new AttributeValue("最大目标数", "max", 1, 0)
        .setTooltip('最多对多少个目标执行子组件')
    );
}

extend('TargetOffset', 'Component');
function TargetOffset()
{
    this.super('Offset', Type.TARGET, true);

    this.description = '以每个目标为基准，取给定偏移处的位置作为目标。';

    this.data.push(new AttributeValue('前向', 'forward', 0, 0)
        .setTooltip('沿目标朝向的偏移距离，负数表示向后。')
    );
    this.data.push(new AttributeValue('上向', 'upward', 2, 0.5)
        .setTooltip('相对目标向上的偏移距离，负数表示向下。')
    );
    this.data.push(new AttributeValue('右向', 'right', 0, 0)
        .setTooltip('相对目标向右的偏移距离，负数表示向左。')
    );
}

extend('TargetRemember', 'Component');
function TargetRemember()
{
    this.super('Remember', Type.TARGET, true);

    this.description = '以「记住目标」组件保存的、键名匹配的实体为目标；若从未保存则失败。';

    this.data.push(new StringValue('键名', 'key', 'target')
        .setTooltip('目标组的唯一键，需与「记住目标」技能中使用的键一致')
    );
}

extend('TargetSelf', 'Component');
function TargetSelf()
{
    this.super('Self', Type.TARGET, true);

    this.description = '把当前目标返回给施法者。';
}

extend('TargetSummonOwner', 'Component');
function TargetSummonOwner()
{
    this.super('Summon Owner', Type.TARGET, true);

    this.description = '获取施法者的主人，如果有的话';
}
// TargetTeamPlayer
extend('TargetTeamPlayer', 'Component');
function TargetTeamPlayer()
{
    this.super('Team Player', Type.TARGET, true);

    this.description = '获取玩家的队友，包含自己';
}

extend('TargetSingle', 'Component');
function TargetSingle()
{
    this.super('Single', Type.TARGET, true);

    this.description = '以当前目标（默认是施法玩家）前方的一个单位为目标。';

    this.data.push(new AttributeValue("范围", "range", 5, 0)
        .setTooltip('目标可处于的最大距离（格）')
    );
    this.data.push(new AttributeValue("容差", "tolerance", 4, 0)
        .setTooltip('瞄准的宽松程度。数值越大越容易命中，本质上是判定锥形的宽度。')
    );
    this.data.push(new ListValue("分组", "group", ["Ally", "Enemy", "Both"], "Enemy")
        .setTooltip('目标筛选方式')
    );
    this.data.push(new ListValue("穿墙", "wall", ['True', 'False'], 'False')
        .setTooltip('是否允许目标位于墙的另一侧')
    );
}

extend('TargetContextArmorStand', 'Component');
function TargetContextArmorStand()
{
    this.super('ContextArmorStand', Type.TARGET, true);

    this.description = '选择技能上下文所创建的盔甲架生物为目标...';

    this.data.push(new ListValue("目标", "target", ['True', 'False'], 'True')
        .setTooltip('如果为FALSE则选择施法者所创建的盔甲架实例...')
    );
    this.data.push(new ListValue("锁定", "lock", ['True', 'False'], 'True')
        .setTooltip('选择盔甲架后，锁定异步任务，尽可能防止重复触发...')
    );
}

// -- Condition constructors --------------------------------------------------- //

extend('ConditionArmor', 'Component');
function ConditionArmor()
{
    this.super('Armor', Type.CONDITION, true);
    this.description = "目标穿着与设定匹配的护甲时执行子组件。";

    this.data.push(new ListValue('护甲', 'armor', [ 'Helmet', 'Chestplate', 'Leggings', 'Boots', 'Any' ], 'Any')
        .setTooltip('需要检查的护甲类型')
    );

    addItemOptions(this);
}

extend('ConditionAttribute', 'Component');
function ConditionAttribute()
{
    this.super('Attribute', Type.CONDITION, true);

    this.description = '要求目标拥有指定数量的属性';

    this.data.push(new StringValue('属性', 'attribute', 'Vitality')
        .setTooltip('要检查的属性名称')
    );
    this.data.push(new AttributeValue('最小', 'min', 0, 0)
        .setTooltip('目标需要达到的属性最小值')
    );
    this.data.push(new AttributeValue('最大', 'max', 999, 0)
        .setTooltip('目标需要达到的属性最大值')
    );
}

extend('ConditionBiome', 'Component');
function ConditionBiome()
{
    this.super('Biome', Type.CONDITION, true);

    this.description = '处于指定生物群系时执行子组件。';

    this.data.push(new ListValue('类型', 'type', [ 'In Biome', 'Not In Biome' ], 'In Biome')
        .setTooltip('目标是否应处于该生物群系。若为「处于」，需位于所选任一生物群系内；若为相反情况，则不能在任何所选群系内。')
    );
    this.data.push(new ByteListValue('生物群系', 'biome', [ 'Beach', 'Desert', 'Forest', 'Frozen', 'Hell', 'Hills', 'Ice', 'Jungle', 'Mesa', 'Mountains', 'Mushroom', 'Ocean', 'Plains', 'Plateau', 'River', 'Savanna', 'Shore', 'Sky', 'Swampland', 'Taiga' ], 1)
        .setTooltip('要检查的生物群系。此处按群系组处理，名称包含该文本的群系都算，例如 JUNGLE_HILLS 同时属于 Jungle 和 Hills')
    );
}

extend('ConditionBlock', 'Component');
function ConditionBlock()
{
    this.super('Block', Type.CONDITION, true);

    this.description = '目标当前站在指定类型的方块上时执行子组件。';

    this.data.push(new ListValue('类型', 'standing', [ 'On Block', 'Not On Block' ], 'On Block')
        .setTooltip('目标是否应处于该生物群系。若为「处于」，需位于所选任一生物群系内；若为相反情况，则不能在任何所选群系内。')
    );
    this.data.push(new ListValue('材质', 'material', materialList, 'Dirt')
        .setTooltip('要求目标站立的方块类型')
    );
}

extend('ConditionCeiling', 'Component');
function ConditionCeiling()
{
    this.super('Ceiling', Type.CONDITION, true);

    this.description = '检测每个目标上方天花板的高度';

    this.data.push(new AttributeValue('距离', 'distance', 5, 0)
        .setTooltip('检测天花板的最高高度')
    );
    this.data.push(new ListValue('至少', 'at-least', [ 'True', 'False' ], 'True')
        .setTooltip('为 true 时天花板至少要有指定格数，为 false 时天花板必须低于指定格数')
    );
}

extend('ConditionChance', 'Component');
function ConditionChance()
{
    this.super('Chance', Type.CONDITION, true);

    this.description = '按几率决定是否执行子组件。';

    this.data.push(new AttributeValue('几率', 'chance', 25, 0)
        .setTooltip('执行子组件的几率（百分比），25 即 25%。')
    );
}

extend('ConditionClass', 'Component');
function ConditionClass()
{
    this.super('Class', Type.CONDITION, true);

    this.description = '目标为指定职业或该职业的进阶职业时执行子组件。例如检测 Fighter（可进阶为 Warrior）时，若未开启 exact，Warrior 也会通过检测。';

    this.data.push(new StringValue('职业', 'class', 'Fighter')
        .setTooltip('玩家需要的职业')
    );
    this.data.push(new ListValue('精确匹配', 'exact', [ 'True', 'False' ], 'False')
        .setTooltip('是否必须为指定职业本身。为 false 时，该职业的进阶职业也算通过。')
    );
}

extend('ConditionClassLevel', 'Component');
function ConditionClassLevel()
{
    this.super('Class Level', Type.CONDITION, true);

    this.description = '该技能所属职业的等级在范围内时执行子组件。只检查施法者等级，不检查目标。';

    this.data.push(new IntValue('最小等级', 'min-level', 2)
        .setTooltip('玩家需要达到的最低职业等级；玩家有多个职业时取当前职业的等级')
    );
    this.data.push(new IntValue('最大等级', 'max-level', 99)
        .setTooltip('玩家允许的最高职业等级；玩家有多个职业时取当前职业的等级')
    );
}

extend('ConditionCombat', 'Component');
function ConditionCombat()
{
    this.super('Combat', Type.CONDITION, true);

    this.description = '目标处于（或脱离）战斗状态时执行子组件。';

    this.data.push(new ListValue('战斗中', 'combat', [ 'True', 'False' ], 'True')
        .setTooltip('目标应处于还是脱离战斗状态')
    );
    this.data.push(new DoubleValue('秒数', 'seconds', 10)
        .setTooltip('最后一次战斗行为后经过多少秒视为脱离战斗')
    );
}

extend('ConditionCrouch', 'Component');
function ConditionCrouch()
{
    this.super('Crouch', Type.CONDITION, true);

    this.description = '目标玩家正在潜行时执行子组件';

    this.data.push(new ListValue('潜行', 'crouch', [ 'True', 'False' ], 'True')
        .setTooltip('玩家是否应处于潜行状态')
    );
}

extend('ConditionDirection', 'Component');
function ConditionDirection()
{
    this.super('Direction', Type.CONDITION, true);

    this.description = '目标或施法者朝向符合设定时执行子组件。';

    this.data.push(new ListValue('类型', 'type', [ 'Target', 'Caster' ], 'Target')
        .setTooltip('要检测朝向的实体')
    );
    this.data.push(new ListValue('方向', 'direction', [ 'Away', 'Towards' ], 'Away')
        .setTooltip('所选实体相对另一方需要面向的方向。')
    );
}

extend('ConditionElevation', 'Component');
function ConditionElevation()
{
    this.super('Elevation', Type.CONDITION, true);

    this.description = '目标的相对高度符合设定时执行子组件。';

    this.data.push(new ListValue('类型', 'type', [ 'Normal', 'Difference' ], 'Normal')
        .setTooltip('比较方式。Normal 直接比较 Y 坐标，Difference 表示与施法者 Y 坐标的差值')
    );
    this.data.push(new AttributeValue('最小值', 'min-value', 0, 0)
        .setTooltip('所需高度的最小值。使用 Difference 时，正值最小值表示目标高于施法者')
    );
    this.data.push(new AttributeValue('最大值', 'max-value', 255, 0)
        .setTooltip('所需高度的最大值。使用 Difference 时，负值最大值表示目标低于施法者')
    );
}

extend('ConditionElse', 'Component');
function ConditionElse()
{
    this.super('Else', Type.CONDITION, true);

    this.description = '前一个组件执行失败时执行子元素。不仅包括条件未通过，也包括因无目标等原因导致的失败。';
}

extend('ConditionEntityType', 'Component');
function ConditionEntityType()
{
    this.super('Entity Type', Type.CONDITION, true);

    this.description = '目标匹配所选实体类型时执行子元素'

    this.data.push(new MultiListValue('类型列表', 'types', [ 'BAT', 'BLAZE', 'CAVE_SPIDER', 'CHICKEN', 'COW', 'CREEPER', 'DONKEY', 'ELDER_GUARDIAN', 'ENDER_DRAGON', 'ENDERMAN', 'ENDERMITE', 'EVOKER', 'GHAST', 'GIANT', 'GUARDIAN', 'HORSE', 'HUSK', 'IRON_GOLEM', 'LLAMA', 'MAGMA_CUBE', 'MULE', 'MUSHROOM_COW', 'OCELOT', 'PIG', 'PIG_ZOMBIE', 'PLAYER', 'POLAR_BEAR', 'RABBIT', 'SHEEP', 'SHULKER', 'SILVERFISH', 'SKELETON', 'SKELETON_HORSE', 'SLIME', 'SNOWMAN', 'SPIDER', 'SQUID', 'VEX', 'VILLAGER', 'VINDICATOR', 'WITCH', 'WITHER', 'WITHER_SKELETON', 'WOLF', 'ZOMBIE', 'ZOMBIE_HORSE', 'ZOMBIE_VILLAGER' ])
        .setTooltip('要作为目标的实体类型')
    );
}

// ConditionEntityCategory
extend('ConditionEntityCategory', 'Component');
function ConditionEntityCategory()
{
    this.super('Entity Category', Type.CONDITION, true);

    this.description = '判断生物分类'

    this.data.push(new MultiListValue('分类', 'category', [ 'PLAYER', 'BOSS', 'MOB' ])
        .setTooltip('分类类型')
    );
}


extend('ConditionEntityExclude', 'Component');
function ConditionEntityExclude()
{
    this.super('Entity Exclude', Type.CONDITION, true);

    this.description = '排除某些种类、名称的生物'

    this.data.push(new MultiListValue('类型列表', 'types', [
        'BAT', 'BLAZE', 'CAVE_SPIDER', 'CHICKEN', 'COW',
        'CREEPER', 'DONKEY', 'ELDER_GUARDIAN',
        'ENDER_DRAGON', 'ENDERMAN', 'ENDERMITE', 'EVOKER',
        'GHAST', 'GIANT', 'GUARDIAN', 'HORSE', 'HUSK',
        'IRON_GOLEM', 'LLAMA', 'MAGMA_CUBE', 'MULE',
        'MUSHROOM_COW', 'OCELOT', 'PIG', 'PIG_ZOMBIE',
        'PLAYER', 'POLAR_BEAR', 'RABBIT', 'SHEEP',
        'SHULKER', 'SILVERFISH', 'SKELETON', 'SKELETON_HORSE',
        'SLIME', 'SNOWMAN', 'SPIDER', 'SQUID', 'VEX', 'VILLAGER',
        'VINDICATOR', 'WITCH', 'WITHER', 'WITHER_SKELETON', 'WOLF',
        'ZOMBIE', 'ZOMBIE_HORSE', 'ZOMBIE_VILLAGER' ]
        ).setTooltip('种类， 可选')
    );
    this.data.push(new StringValue('名称', 'names', '')
        .setTooltip('生物名字， 可选')
    );

}

extend('ConditionFire', 'Component');
function ConditionFire()
{
    this.super('Fire', Type.CONDITION, true);

    this.description = '目标着火时执行子组件。';

    this.data.push(new ListValue('类型', 'type', [ 'On Fire', 'Not On Fire' ], 'On Fire')
        .setTooltip('目标是否应处于着火状态')
    );
}

extend('ConditionFlag', 'Component');
function ConditionFlag()
{
    this.super('Flag', Type.CONDITION, true);

    this.description = '目标带有对应标记时执行子组件。';

    this.data.push(new ListValue('类型', 'type', [ 'Set', 'Not Set' ], 'Set')
        .setTooltip('该标记是否应处于设置状态')
    );
    this.data.push(new StringValue('键名', 'key', 'key')
        .setTooltip('标记的唯一键，需与通过 Flag 或 Flag Toggle 组件设置时使用的键一致')
    );
}

extend('ConditionGround', 'Component');
function ConditionGround()
{
    this.super('Ground', Type.CONDITION, true);

    this.description = '目标站在地面上时执行子组件';

    this.data.push(new ListValue('类型', 'type', [ 'On Ground', 'Not On Ground' ], 'On Ground')
        .setTooltip('目标是否应站在地面上')
    );
}

extend('ConditionHealth', 'Component');
function ConditionHealth()
{
    this.super('Health', Type.CONDITION, true);

    this.description = "目标生命值符合设定时执行子组件。";

    this.data.push(new ListValue('类型', 'type', [ 'Health', 'Percent', 'Difference', 'Difference Percent' ], 'Health')
        .setTooltip('生命值的比较方式。Health 为剩余生命值，Percent 为剩余生命百分比，Difference 为目标与施法者生命值的差值，Difference percent 为两者剩余生命百分比的差值')
    );
    this.data.push(new AttributeValue('最小值', 'min-value', 0, 0)
        .setTooltip('所需的最小生命值。使用 Difference 类型时，正值最小值表示目标生命值更高')
    );
    this.data.push(new AttributeValue('最大值', 'max-value', 10, 2)
        .setTooltip('所需的最大生命值。使用 Difference 类型时，负值最大值表示目标生命值更低')
    );
}

extend('ConditionItem', 'Component');
function ConditionItem()
{
    this.super('Item', Type.CONDITION, true);
    this.description = "目标手持与设定材质匹配的物品时执行子组件。";

    addItemOptions(this);
}

extend('ConditionInventory', 'Component');
function ConditionInventory()
{
    this.super('Inventory', Type.CONDITION, true);

    this.description = '目标玩家背包中拥有指定物品时执行子组件，对怪物无效。';

    this.data.push(new AttributeValue('数量', 'amount', 1, 0)
        .setTooltip('玩家背包中所需的物品数量')
    );

    addItemOptions(this);
}

extend('ConditionLight', 'Component');
function ConditionLight()
{
    this.super('Light', Type.CONDITION, true);

    this.description = "目标所在位置的光照等级符合设定时执行子组件。";

    this.data.push(new AttributeValue('最小光照', 'min-light', 0, 0)
        .setTooltip('所需的最低光照等级，16 为最亮，0 为全黑')
    );
    this.data.push(new AttributeValue('最大光照', 'max-light', 16, 16)
        .setTooltip('所需的最高光照等级，16 为最亮，0 为全黑')
    );
}

extend('ConditionMana', 'Component');
function ConditionMana() {
    this.super('Mana', Type.CONDITION, true);

    this.description = "目标法力值符合设定时执行子组件。";

    this.data.push(new ListValue('类型', 'type', [ 'Mana', 'Percent', 'Difference', 'Difference Percent' ], 'Mana')
        .setTooltip('法力的比较方式。Mana 为剩余法力，Percent 为剩余法力百分比，Difference 为目标与施法者法力的差值，Difference percent 为两者剩余法力百分比的差值')
    );
    this.data.push(new AttributeValue('最小值', 'min-value', 0, 0)
        .setTooltip('所需的最低法力值')
    );
    this.data.push(new AttributeValue('最大值', 'max-value', 10, 2)
        .setTooltip('所需的最高法力值')
    );
    this.data.push(new StringValue('tick', 'tick', "1000")
        .setTooltip('设置法力值恢复延后，1000 = 1秒')
    );
}

extend('ConditionName', 'Component');
function ConditionName()
{
    this.super('Name', Type.CONDITION, true);

    this.description = '目标名称符合设定时执行子组件。';

    this.data.push(new ListValue('包含文本', 'contains', [ 'True', 'False' ], 'True')
        .setTooltip('目标名称是否应包含该文本')
    );
    this.data.push(new ListValue('正则', 'regex', [ 'True', 'False' ], 'False')
        .setTooltip('该文本是否按正则表达式处理，不了解正则可忽略此选项')
    );
    this.data.push(new StringValue('文本', 'text', 'text')
        .setTooltip('要在目标名称中查找的文本')
    );
}

extend('ConditionOffhand', 'Component');
function ConditionOffhand()
{
    this.super('Offhand', Type.CONDITION, true);
    this.description = "目标副手持与设定材质匹配的物品时执行子组件，仅 1.9+ 服务端有效。";

    addItemOptions(this);
}

extend('ConditionPermission', 'Component');
function ConditionPermission()
{
    this.super('Permission', Type.CONDITION, true);

    this.description = '施法者拥有所需权限时执行子组件';

    this.data.push(new StringValue('权限', 'perm', 'some.permission')
        .setTooltip('玩家需要的权限')
    );
}

extend('ConditionPotion', 'Component');
function ConditionPotion()
{
    this.super('Potion', Type.CONDITION, true);

    this.description = '目标拥有该药水效果时执行子组件。';

    this.data.push(new ListValue('类型', 'type', [ 'Active', 'Not Active' ], 'Active')
        .setTooltip('该药水效果是否应处于激活状态')
    );
    this.data.push(new ListValue('药水', 'potion', [ 'Any', 'Absorption', 'Blindness', 'Confusion', 'Damage Resistance', 'Fast Digging', 'Fire Resistance', 'Glowing', 'Health Boost', 'Hunger', 'Increase Damage', 'Invisibility', 'Jump', 'Levitation', 'Luck', 'Night Vision', 'Poison', 'Regeneration', 'Saturation', 'Slow', 'Slow Digging', 'Speed', 'Unluck', 'Water Breathing', 'Weakness', 'Wither' ], 'Any')
        .setTooltip('要检查的药水类型')
    );
    this.data.push(new AttributeValue('最小阶级', 'min-rank', 0, 0)
        .setTooltip('药水效果的最低等级')
    );
    this.data.push(new AttributeValue('最大阶级', 'max-rank', 999, 0)
        .setTooltip('药水效果的最高等级')
    );
}

extend('ConditionSkillLevel', 'Component');
function ConditionSkillLevel(skill)
{
    this.super('Skill Level', Type.CONDITION, true);

    this.description = '技能等级在范围内时执行子组件。只检查施法者的技能等级，不检查目标。';

    this.data.push(new StringValue('技能', 'skill', skill)
        .setTooltip('要检查等级的技能名称。若要检查当前技能，也要填写当前技能的名字')
    );
    this.data.push(new IntValue('最小等级', 'min-level', 2)
        .setTooltip('所需的技能最低等级')
    );
    this.data.push(new IntValue('最大等级', 'max-level', 99)
        .setTooltip('所需的技能最高等级')
    );
}

extend('ConditionShield', 'Component');
function ConditionShield()
{
    this.super('Shield', Type.CONDITION, true);

    this.description = '判断目标或者施法者是否是举盾状态';

    this.data.push(new ListValue('目标', 'target', [ 'True', 'False' ], 'True')
        .setTooltip('是否取目标举盾状态')
    );
}

extend('ConditionSlot', 'Component');
function ConditionSlot()
{
    this.super('Slot', Type.CONDITION, true);
    this.description = "目标玩家在指定槽位拥有匹配物品时执行子组件。";

    this.data.push(new StringListValue('槽位（每行一个）', 'slot', [9])
        .setTooltip('要检查的槽位。0-8 为快捷栏，9-35 为背包，36-39 为护甲，40 为副手；可填多个槽位，任一匹配即通过。')
    );

    addItemOptions(this);
}

extend('ConditionStatus', 'Component');
function ConditionStatus()
{
    this.super('Status', Type.CONDITION, true);

    this.description = '目标拥有该状态时执行子组件。';

    this.data.push(new ListValue('类型', 'type', [ 'Active', 'Not Active' ], 'Active')
        .setTooltip('该状态是否应处于激活状态')
    );
    this.data.push(new ListValue('状态', 'status', [ 'Any', 'Absorb', 'Curse', 'Disarm', 'Invincible', 'Root', 'Silence', 'Stun' ], 'Any')
        .setTooltip('要检查的状态')
    );
}

extend('ConditionTime', 'Component');
function ConditionTime()
{
    this.super('Time', Type.CONDITION, true);

    this.description = '服务器时间符合设定时执行子组件。';

    this.data.push(new ListValue('时间', 'time', [ 'Day', 'Night' ], 'Day')
        .setTooltip('要检查的当前世界时间')
    );
}

extend('ConditionTool', 'Component');
function ConditionTool()
{
    this.super('Tool', Type.CONDITION, true);

    this.description = '目标手持匹配的工具时执行子组件。';

    this.data.push(new ListValue('材质', 'material', [ 'Any', 'Wood', 'Stone', 'Iron', 'Gold', 'Diamond' ], 'Any')
        .setTooltip('手持工具需要的材质')
    );
    this.data.push(new ListValue('工具', 'tool', [ 'Any', 'Axe', 'Hoe', 'Pickaxe', 'Shovel', 'Sword' ], 'Any')
        .setTooltip('需要的工具类型')
    );
}

extend('ConditionValue', 'Component');
function ConditionValue()
{
    this.super('Value', Type.CONDITION, true);

    this.description = '存储值在给定范围内时执行子组件。';

    this.data.push(new StringValue('键名', 'key', 'value')
        .setTooltip('由 Value 组件保存该值时使用的唯一字符串')
    );
    this.data.push(new AttributeValue('最小值', 'min-value', 1, 0)
        .setTooltip('所需值的下限')
    );
    this.data.push(new AttributeValue('最大值', 'max-value', 999, 0)
        .setTooltip('所需值的上限')
    );
}

extend('ConditionWater', 'Component');
function ConditionWater()
{
    this.super('Water', Type.CONDITION, true);

    this.description = '目标处于水中或离开水面时执行子组件。';

    this.data.push(new ListValue('状态', 'state', [ 'In Water', 'Out Of Water' ], 'In Water')
        .setTooltip('目标是否需要在水中')
    );
}

extend('ConditionWeather', 'Component');
function ConditionWeather() {
    this.super('Weather', Type.CONDITION, true);

    this.description = '目标所在位置的天气符合设定时执行子组件';

    this.data.push(new ListValue('类型', 'type', [ 'None', 'Rain', 'Snow', 'Thunder' ], 'Rain')
        .setTooltip('目标是否需要在水中')
    );
}

extend('ConditionData', 'Component');
function ConditionData() {
    this.super('Data', Type.CONDITION, true);

    this.description = '自定义临时数据条件';
    this.data.push(new StringValue('键名', 'key', 'key')
    );
    this.data.push(new AttributeValue('最小值', 'min-value', 1, 0)
    );
    this.data.push(new AttributeValue('最大值', 'max-value', 999, 0)
    );
}

extend('ConditionWorld', 'Component');
function ConditionWorld() {
    this.super('World', Type.CONDITION, true);

    this.description = '世界、目标排除器';

    // 指定世界
    this.data.push(new StringValue('世界', 'world', 'world')
        .setTooltip('指定的世界名，如果为空则跳过判定，可以输入世界名，例如 "world" 或 "world_nether"')
    );

    // 是否以目标为基准
    this.data.push(new ListValue('使用目标', 'use-target', [ 'True', 'False' ], 'True')
        .setTooltip('是否以目标所在世界为准，否则以施法者所在世界为准')
    );

    // 是否要求必须匹配世界
    this.data.push(new ListValue('需要匹配', 'require-match', [ 'True', 'False' ], 'True')
        .setTooltip('True=必须在该世界，False=必须不在该世界')
    );

    // 是否排除玩家
    this.data.push(new ListValue('排除玩家', 'exclude-player', [ 'True', 'False' ], 'False')
        .setTooltip('世界条件达成后，如果为 True，则技能不会对玩家生效')
    );
}

//ConditionMythicSummonSurvival
extend('ConditionMythicSummonSurvival', 'Component');
function ConditionMythicSummonSurvival() {
    this.super('Mythic Summon Survival', Type.CONDITION, true);

    this.description = '判断目标指定召唤物数量';

    // 指定世界
    this.data.push(new StringValue('神话类型', 'mythicType', 'default')
        .setTooltip('MM 生物配置的 ID 非怪物展示名称')
    );

    this.data.push(new AttributeValue('最小数量', 'minAmount', 1, 0)
        .setTooltip('要求的最小存在数量')
    );
    this.data.push(new AttributeValue('最大数量', 'maxAmount', 999, 0)
        .setTooltip('要求的最大存在数量')
    );
    this.data.push(new AttributeValue('范围', 'range', 10.0, 0)
        .setTooltip('范围、可选')
    );

}

extend('ConditionAreaEntity', 'Component');
function ConditionAreaEntity() {
    this.super('Area Entity', Type.CONDITION, true);

    this.description = '范围内敌对生物';

    // 指定世界
    this.data.push(new ListValue('类型', 'type', ["ally", "all", "ot"], "all")
        .setTooltip('ally = 要求是盟友, all = 任意,ot = 敌对')
    );

    this.data.push(new AttributeValue('最小数量', 'minAmount', 1, 0)
        .setTooltip('要求的最小存在数量')
    );
    this.data.push(new AttributeValue('最大数量', 'maxAmount', 999, 0)
        .setTooltip('要求的最大存在数量')
    );
    this.data.push(new AttributeValue('范围', 'range', 10.0, 0)
        .setTooltip('范围')
    );

}

extend('ConditionHealthScale', 'Component');
function ConditionHealthScale() {
    this.super('Health Scale', Type.CONDITION, true);

    this.description = '血量比例';

    // 指定世界
    this.data.push(new ListValue('低于比例', 'much', ["true", "False"], 'False')
        .setTooltip('true = 血量低于 scale; false = 高于 scale')
    );
    this.data.push(new AttributeValue('缩放', 'scale', 0.0, 0)
        .setTooltip('比例')
    );

}

extend('ConditionHealthComparison', 'Component');
function ConditionHealthComparison() {
    this.super('Health Comparison', Type.CONDITION, true);

    this.description = '血量比较';

    // 指定世界
    this.data.push(new ListValue('方向', 'direction', ["higher", "lower"], 'higher')
        .setTooltip('比较方向：higher / lower')
    );
    this.data.push(new AttributeValue('缩放', 'scale', 0.0, 0)
        .setTooltip('比例')
    );

}

extend('ConditionHealthInterval', 'Component');
function ConditionHealthInterval() {
    this.super('Health Interval', Type.CONDITION, true);

    this.description = '血量区间比较';

    this.data.push(new AttributeValue('最小', 'min', 0.3, 0)
        .setTooltip('最小值')
    );
    this.data.push(new AttributeValue('最大', 'max', 0.5, 0)
        .setTooltip('最大值')
    );

}

// -- Mechanic constructors ---------------------------------------------------- //

extend('MechanicAttribute', 'Component');
function MechanicAttribute()
{
    this.super('Attribute', Type.MECHANIC, false);

    this.description = '临时给予玩家属性加成。';

    this.data.push(new StringValue('属性', 'key', 'Intelligence')
        .setTooltip('要增加的属性名称')
    );
    this.data.push(new AttributeValue('数量', 'amount', 5, 2)
        .setTooltip('增加多少属性值')
    );
    this.data.push(new AttributeValue('秒数', 'seconds', 3, 0)
        .setTooltip('属性加成持续多少秒')
    );
    this.data.push(new ListValue('可堆叠', 'stackable', [ 'True', 'False' ], 'False')
        .setTooltip('[高级] 多次执行是否叠加效果')
    );
}

extend('MechanicBlock', 'Component');
function MechanicBlock() {
    this.super('Block', Type.MECHANIC, false);

    this.description = '在限定时间内把方块变为指定类型。';

    this.data.push(new ListValue('形状', 'shape', [ 'Sphere', 'Cuboid' ], 'Sphere' )
        .setTooltip('要改变方块区域的形状')
    );
    this.data.push(new ListValue('类型', 'type', [ 'Air', 'Any', 'Solid' ], 'Solid' )
        .setTooltip('要替换的方块类型。Air 或 any 用于制造障碍，solid 则改变现有环境')
    );
    this.data.push(new ListValue('方块', 'block', materialList, 'Ice')
        .setTooltip('区域要变成的方块类型')
    );
    this.data.push(new IntValue('方块数据', 'data', 0)
        .setTooltip('方块数据值，多用于告示牌、木头、台阶等')
    );
    this.data.push(new AttributeValue('秒数', 'seconds', 5, 0)
        .setTooltip('方块被替换后持续的时间')
    );
    this.data.push(new AttributeValue('前向偏移', 'forward', 0, 0)
        .setTooltip('区域在目标前方多少格，负值表示在身后')
    );
    this.data.push(new AttributeValue('上向偏移', 'upward', 0, 0)
        .setTooltip('区域在目标上方多少格，负值表示在下方')
    );
    this.data.push(new AttributeValue('右偏移', 'right', 0, 0)
        .setTooltip('区域在目标右侧多少格，负值表示在左侧')
    );

    // Sphere options
    this.data.push(new AttributeValue('半径', 'radius', 3, 0).requireValue('shape', [ 'Sphere' ])
        .setTooltip('球形区域的半径（格）')
    );

    // Cuboid options
    this.data.push(new AttributeValue('宽度 (X)', 'width', 5, 0).requireValue('shape', [ 'Cuboid' ])
        .setTooltip('长方体的宽度（格）')
    );
    this.data.push(new AttributeValue('高度 (Y)', 'height', 5, 0).requireValue('shape', [ 'Cuboid' ])
        .setTooltip('长方体的高度（格）')
    );
    this.data.push(new AttributeValue('深度 (Z)', 'depth', 5, 0).requireValue('shape', [ 'Cuboid' ])
        .setTooltip('长方体的深度（格）')
    );
}

extend('MechanicBlockWall', 'Component');
function MechanicBlockWall() {
    this.super('Block Wall', Type.MECHANIC, false);
    this.description = '方块墙，并且召唤盔甲架';
    this.data.push(new ListValue('方块', 'block', materialList, 'Stone')
        .setTooltip('组成墙的方块')
    );
    this.data.push(new AttributeValue('秒数', 'seconds', 5, 0)
        .setTooltip('恢复时间')
    );
    this.data.push(new IntValue('长度', 'length', 1)
        .setTooltip('墙半径长度')
    );
    this.data.push(new IntValue('高度', 'height', 0)
        .setTooltip('墙高度')
    );
    this.data.push(new StringValue('名称', 'name', 'Armor Stand Packet')
        .setTooltip('盔甲架名称')
    );
}

extend('MechanicBuff', 'Component');
function MechanicBuff()
{
    this.super('Buff', Type.MECHANIC, false);

    this.description = '为目标提供战斗属性增益';

    this.data.push(new ListValue('立即', 'immediate', [ 'True', 'False' ], 'False')
        .setTooltip('是否把增益应用到当前伤害触发器上')
    );
    this.data.push(new ListValue('类型', 'type', [ 'DAMAGE', 'DEFENSE', 'SKILL_DAMAGE', 'SKILL_DEFENSE', 'HEALING' ], 'DAMAGE')
        .requireValue('immediate', [ 'False' ])
        .setTooltip('增益类型。DAMAGE/DEFENSE 作用于普通攻击，SKILL_DAMAGE/SKILL_DEFENSE 作用于技能伤害，HEALING 作用于技能治疗')
    );
    this.data.push(new ListValue('修饰符', 'modifier', [ 'Flat', 'Multiplier' ], 'Flat')
        .setTooltip('增益的计算方式。Flat 以固定数值增减，Multiplier 按伤害百分比增减；倍率大于 1 提高承伤，小于 1 降低承伤。')
    );
    this.data.push(new StringValue('分类', 'category', '')
        .requireValue('type', [ 'SKILL_DAMAGE', 'SKILL_DEFENSE' ])
        .setTooltip('作用的技能伤害类型，留空表示所有技能伤害。')
    );
    this.data.push(new AttributeValue('数值', 'value', 1, 0)
        .setTooltip('增减的数值')
    );
    this.data.push(new AttributeValue('秒数', 'seconds', 3, 0)
        .requireValue('immediate', [ 'False' ])
        .setTooltip('增益持续时间（秒）')
    );
}

extend('MechanicCancel', 'Component');
function MechanicCancel()
{
    this.super('Cancel', Type.MECHANIC, false);

    this.description = '取消触发该组件所在触发器的事件。例如伤害类触发器会取消已造成的伤害，Launch 触发器会取消抛射物的发射。';
}

extend('MechanicCancelEffect', 'Component');
function MechanicCancelEffect()
{
    this.super('Cancel Effect', Type.MECHANIC, false);

    this.description = '提前停止粒子特效。';

    this.data.push(new StringValue('特效键', 'effect-key', 'default')
        .setTooltip('设置该特效时使用的键')
    );
}

extend('MechanicChannel', 'Component');
function MechanicChannel()
{
    this.super('Channel', Type.MECHANIC, true);

    this.description = '经过一段时间后执行子效果，期间可被打断。引导期间玩家无法移动、攻击或施放其它技能。';

    this.data.push(new ListValue('静止', 'still', [ 'True', 'False' ], 'True')
        .setTooltip('引导期间是否固定玩家位置')
    );
    this.data.push(new AttributeValue('时间', 'time', 3, 0)
        .setTooltip('引导持续时间（秒）')
    );
}

extend('MechanicCleanse', 'Component');
function MechanicCleanse()
{
    this.super('Cleanse', Type.MECHANIC, false);

    this.description = '清除目标身上的负面药水效果或状态。';

    this.data.push(new ListValue('药水', 'potion', [ 'None', 'All', 'Blindness', 'Confusion', 'Hunger', 'Levitation', 'Poison', 'Slow', 'Slow Digging', 'Weakness', 'Wither' ], 'All')
        .setTooltip('要从目标身上移除的药水效果类型')
    );
    this.data.push(new ListValue('状态', 'status', [ 'None', 'All', 'Curse', 'Disarm', 'Root', 'Silence', 'Stun' ], 'All')
        .setTooltip('要从目标身上移除的状态')
    );
}

extend('MechanicCommand', 'Component');
function MechanicCommand()
{
    this.super('Command', Type.MECHANIC, false);

    this.description ='以目标身份执行指令，可通过临时 OP 或控制台代其执行。';

    this.data.push(new StringValue('指令', 'command', '')
        .setTooltip('要执行的指令')
    );
    this.data.push(new ListValue('执行方式', 'type', [ 'Console', 'OP' ], 'OP')
        .setTooltip('指令执行方式。Console 由控制台执行，OP 则临时给予目标玩家 OP 权限后由其执行；可用 {player} 插入目标玩家名称')
    );
}

extend('MechanicCooldown', 'Component');
function MechanicCooldown()
{
    this.super('Cooldown', Type.MECHANIC, false);

    this.description = "降低目标技能的冷却；填写负值则会增加冷却。";

    this.data.push(new StringValue('技能（或 "all"）', 'skill', 'all')
        .setTooltip('要调整冷却的技能')
    );
    this.data.push(new ListValue('类型', 'type', [ 'Seconds', 'Percent' ], 'Seconds')
        .setTooltip('调整单位。Seconds 按秒增减冷却，Percent 按总冷却的百分比增减')
    );
    this.data.push(new AttributeValue('数值', 'value', -1, 0)
        .setTooltip('增减的数值')
    );
}

extend('MechanicDamage', 'Component');
function MechanicDamage()
{
    this.super('Damage', Type.MECHANIC, false);

    this.description = '对每个目标造成技能伤害。Multiplier 类型为最大生命值的百分比。';

    this.data.push(new ListValue('类型', 'type', [ 'Damage', 'Multiplier', 'Percent Left', 'Percent Missing' ], 'Damage')
        .setTooltip('伤害数值的单位。Damage 为固定伤害，Multiplier 为目标最大生命值的百分比，Percent Left 为当前生命值的百分比，Percent Missing 为已损失生命值的百分比')
    );
    this.data.push(new AttributeValue("数值", "value", 3, 1)
        .setTooltip('造成的伤害数值')
    );
    this.data.push(new ListValue('真实伤害', 'true', [ 'True', 'False' ], 'False')
        .setTooltip('是否造成真实伤害。真实伤害无视护甲与插件判定。')
    );
    this.data.push(new StringValue('分类器', 'classifier', 'default')
        .setTooltip('[仅高级版] 造成的伤害类型，可作为元素伤害或模拟物理伤害')
    );
    this.data.push(new ListValue('击退', 'true', [ 'True', 'False' ], 'False')
        .setTooltip('是否击退，准确的来说该攻击是否有攻击源，也就是攻击生物')
    );
}

extend('MechanicDamageBuff', 'Component');
function MechanicDamageBuff()
{
    this.super('Damage Buff', Type.MECHANIC, false);

    this.description = '在限定时间内以倍数或固定值修改每个目标造成的物理伤害。负值或小于 1 的倍率会降低伤害，反之提高伤害（例如 5% 增伤对应倍率 1.05）';

    this.data.push(new ListValue('类型', 'type', [ 'Flat', 'Multiplier' ], 'Flat')
        .setTooltip('增益计算方式。Flat 以固定数值增减，Multiplier 按百分比增减。')
    );
    this.data.push(new ListValue('技能伤害', 'skill', [ 'True', 'False' ], 'False')
        .setTooltip('是否作用于技能伤害，为 false 时作用于物理伤害。')
    );
    this.data.push(new AttributeValue('数值', 'value', 1, 0)
        .setTooltip('增减的数值。Flat 类型下负值表示降低伤害，相当于倍率小于 1。')
    );
    this.data.push(new AttributeValue('秒数', 'seconds', 3, 0)
        .setTooltip('增益持续时间（秒）')
    );
}

extend('MechanicDamageLore', 'Component');
function MechanicDamageLore()
{
    this.super('Damage Lore', Type.MECHANIC, false);

    this.description = '根据施法者手持物品 Lore 中的数值对每个目标造成伤害。';

    this.data.push(new ListValue("手", "hand", [ 'Main', 'Offhand' ], 'Main')
        .setTooltip('检查物品的手，副手仅 1.9+ 版本可用。')
    );
    this.data.push(new StringValue('正则', 'regex', 'Damage: {value}')
        .setTooltip('要查找文本的正则表达式。用 {value} 标记数值所在位置；不熟悉正则可在网上查阅，或避免使用 [ ] { } ( ) . + ? * ^ \\ | 等特殊字符')
    );
    this.data.push(new AttributeValue('倍率', 'multiplier', 1, 0)
        .setTooltip('数值的倍率，用于计算实际伤害')
    );
    this.data.push(new ListValue('真实伤害', 'true', [ 'True', 'False' ], 'False')
        .setTooltip('是否造成真实伤害。真实伤害无视护甲与插件判定。')
    );
    this.data.push(new StringValue('分类器', 'classifier', 'default')
        .setTooltip('[仅高级版] 造成的伤害类型，可作为元素伤害或模拟物理伤害')
    );
}

extend('MechanicDefenseBuff', 'Component');
function MechanicDefenseBuff()
{
    this.super('Defense Buff', Type.MECHANIC, false);

    this.description = '在限定时间内以倍数或固定值修改每个目标受到的物理伤害。负值或小于 1 的倍率会降低承伤，反之提高承伤（例如 5% 减伤对应倍率 0.95，即只承受 95% 伤害）';

    this.data.push(new ListValue('类型', 'type', [ 'Flat', 'Multiplier' ], 'Flat')
        .setTooltip('增益计算方式。Flat 以固定数值增减，Multiplier 按伤害百分比增减；倍率大于 1 提高承伤，小于 1 降低承伤。')
    );
    this.data.push(new ListValue('技能防御', 'skill', [ 'True', 'False' ], 'False')
        .setTooltip('是否作用于技能防御，为 false 时作用于物理防御。')
    );
    this.data.push(new AttributeValue('数值', 'value', 1, 0)
        .setTooltip('增减的数值')
    );
    this.data.push(new AttributeValue('秒数', 'seconds', 3, 0)
        .setTooltip('增益持续时间（秒）')
    );
}

extend('MechanicDelay', 'Component');
function MechanicDelay()
{
    this.super('Delay', Type.MECHANIC, true);

    this.description = '经过一段延迟后执行子组件。';

    this.data.push(new AttributeValue('延迟', 'delay', 2, 0)
        .setTooltip('执行子组件前等待的秒数')
    );

    this.data.push(new StringValue('标记', 'mark', '标记名称')
        .setTooltip('为 delay 延时程序设置唯一标签')
    );

    this.data.push(new StringValue('禁止发包行为', 'deny-germ-action', '')
        .setTooltip('当 delay 被打断时为目标执行的动作')
    );

    this.data.push(new StringValue('禁止指令', 'deny-commands', '')
        .setTooltip('当 delay 被打断时为目标执行的指令')
    );
}

extend('MechanicDisguise', 'Component');
function MechanicDisguise()
{
    this.super('Disguise', Type.MECHANIC, false);

    this.description = '按设定伪装每个目标，需要服务器安装 LibsDisguise 插件。';

    this.data.push(new AttributeValue('持续时间', 'duration', -1, 0)
        .setTooltip('伪装持续时间（秒），负数表示永久伪装。')
    );
    this.data.push(new ListValue('类型', 'type', [ 'Mob', 'Player', 'Misc' ], 'Mob')
        .setTooltip('伪装类型，由 LibsDisguise 插件定义。')
    );

    this.data.push(new ListValue('生物', 'mob', [ 'Bat', 'Blaze', 'Cave Spider', 'Chicken', 'Cow', 'Creeper', 'Donkey', 'Elder Guardian', 'Ender Dragon', 'Enderman', 'Endermite', 'Ghast', 'Giant', 'Guardian', 'Horse', 'Iron Golem', 'Magma Cube', 'Mule', 'Mushroom Cow', 'Ocelot', 'Pig', 'Pig Zombie', 'Rabbit', 'Sheep', 'Shulker', 'Silverfish', 'Skeleton', 'Slime', 'Snowman', 'Spider', 'Squid', 'Undead Horse', 'Villager', 'Witch', 'Wither', 'Wither Skeleton', 'Wolf', 'Zombie', 'Zombie Villager'], 'Zombie')
        .requireValue('type', [ 'Mob' ])
        .setTooltip('伪装成的生物类型')
    );
    this.data.push(new ListValue('成年', 'adult', [ 'True', 'False', ], 'True')
        .requireValue('type', [ 'Mob' ])
        .setTooltip('是否使用该生物的成年形态')
    );

    this.data.push(new StringValue('玩家', 'player', 'Eniripsa96')
        .requireValue('type', [ 'Player' ])
        .setTooltip('伪装成的玩家')
    );

    this.data.push(new ListValue('杂项', 'misc', [ 'Area Effect Cloud', 'Armor Stand', 'Arrow', 'Boat', 'Dragon Fireball', 'Dropped Item', 'Egg', 'Ender Crystal', 'Ender Pearl', 'Ender Signal', 'Experience Orb', 'Falling Block', 'Fireball', 'Firework', 'Fishing Hook', 'Item Frame', 'Leash Hitch', 'Minecart', 'Minecart Chest', 'Minecart Command', 'Minecart Furnace', 'Minecart Hopper', 'Minecart Mob Spawner', 'Minecart TNT', 'Painting', 'Primed TNT', 'Shulker Bullet', 'Snowball', 'Spectral Arrow', 'Splash Potion', 'Tipped Arrow', 'Thrown EXP Bottle', 'Wither Skull' ], 'Painting')
        .requireValue('type', [ 'Misc' ])
        .setTooltip('伪装成的对象')
    );
    this.data.push(new IntValue('数据', 'data', 0)
        .requireValue('type', [ 'Misc' ])
        .setTooltip('伪装类型对应的数据值，具体作用取决于伪装类型')
    );
}

extend('MechanicDurability', 'Component');
function MechanicDurability()
{
    this.super('Durability', Type.MECHANIC, false);

    this.description = '降低手持物品的耐久';

    this.data.push(new AttributeValue('数量', 'amount', 1, 0)
        .setTooltip('降低的耐久值')
    );
    this.data.push(new ListValue('副手', 'offhand', [ 'True', 'False' ], 'False')
        .setTooltip('是否作用于副手槽位')
    );
}

extend('MechanicExplosion', 'Component');
function MechanicExplosion()
{
    this.super('Explosion', Type.MECHANIC, false);

    this.description = '在当前目标位置引发爆炸';

    this.data.push(new AttributeValue('强度', 'power', 3, 0)
        .setTooltip('爆炸强度')
    );
    this.data.push(new ListValue('破坏方块', 'damage', [ 'True', 'False' ], 'False')
        .setTooltip('爆炸是否破坏方块')
    );
    this.data.push(new ListValue('着火', 'fire', [ 'True', 'False' ], 'False')
        .setTooltip('是否点燃受影响的方块')
    );
}

extend('MechanicFire', 'Component');
function MechanicFire()
{
    this.super('Fire', Type.MECHANIC, false);

    this.description = '使目标着火一段时间。';

    this.data.push(new AttributeValue('秒数', 'seconds', 3, 1)
        .setTooltip('着火持续时间（秒）')
    );
}

extend('MechanicFlag', 'Component');
function MechanicFlag()
{
    this.super('Flag', Type.MECHANIC, false);

    this.description = '为目标添加一个限时标记。标记可被其它触发器、技能或相关组件检测，用于联动效果。';

    this.data.push(new StringValue('键名', 'key', 'key')
        .setTooltip('标记的唯一字符串，在 Flag 条件中检查时使用同一个键。')
    );
    this.data.push(new AttributeValue('秒数', 'seconds', 3, 1)
        .setTooltip('标记持续时间。若要永久保持，请使用 Flag Toggle。')
    );
}


extend('MechanicFlagReduce', 'Component');
function MechanicFlagReduce()
{
    this.super('Flag Reduce', Type.MECHANIC, false);

    this.description = '设置特定flag的冷却缩减比例.';

    this.data.push(new StringListValue('键名', 'key', [ 'Default' ])
        .setTooltip('要进行缩减的 flag 名称')
    );
    this.data.push(new AttributeValue('缩放', 'scale', 0.5, 0.0)
        .setTooltip('缩减比例 <0.0-1.0> 其中 0.5=50%')
    );
}

extend('MechanicFlagClear', 'Component');
function MechanicFlagClear()
{
    this.super('Flag Clear', Type.MECHANIC, false);

    this.description = '清除目标身上的标记。';

    this.data.push(new StringValue('键名', 'key', 'key')
        .setTooltip('标记的唯一字符串，需与最初设置该标记的组件一致。')
    );
}

extend('MechanicFlagToggle', 'Component');
function MechanicFlagToggle()
{
    this.super('Flag Toggle', Type.MECHANIC, false);

    this.description = '切换目标身上的标记开关，可用于制作开关类效果。';

    this.data.push(new StringValue('键名', 'key', 'key')
        .setTooltip('标记的唯一字符串，在 Flag 条件中检查时使用同一个键')
    );
}

extend('MechanicFood', 'Component');
function MechanicFood()
{
    this.super('Food', Type.MECHANIC, false);

    this.description = '增减玩家的饥饿值与饱和度';

    this.data.push(new AttributeValue('饥饿值', 'food', 1, 1)
        .setTooltip('给予的饥饿值数量，负值表示降低饥饿值')
    );
    this.data.push(new AttributeValue('饱和度', 'saturation', 0, 0)
        .setTooltip('给予的饱和度数量，负值表示降低饱和度。这是隐藏数值，决定饥饿值多久开始下降。')
    );
}

extend('MechanicForgetTargets', 'Component');
function MechanicForgetTargets()
{
    this.super('Forget Targets', Type.MECHANIC, false);

    this.description = '清除由「记住目标」组件保存的目标';

    this.data.push(new StringValue('键名', 'key', 'key')
        .setTooltip('保存目标时使用的唯一键')
    );
}

extend('MechanicHeal', 'Component');
function MechanicHeal()
{
    this.super('Heal', Type.MECHANIC, false);

    this.description = '为每个目标恢复生命值。';

    this.data.push(new ListValue("类型", "type", [ "Health", "Percent" ], "Health")
        .setTooltip('恢复生命值的单位。Health 为固定数值，Percent 为最大生命值的百分比。')
    );
    this.data.push(new AttributeValue("数值", "value", 3, 1)
        .setTooltip('恢复的生命值数量')
    );
}

extend('MechanicHealthSet', 'Component');
function MechanicHealthSet()
{
    this.super('Health Set', Type.MECHANIC, false);

    this.description = '把目标生命值设为指定数值，无视抗性、伤害增益等';

    this.data.push(new AttributeValue("生命值", "health", 1, 0)
        .setTooltip('要设置的生命值')
    );
}

// MechanicHealth
extend('MechanicHealth', 'Component');
function MechanicHealth()
{
    this.super('Health', Type.MECHANIC, false);

    this.description = '根据参数修改生命值';

    // 基准值输入（固定值、百分比基准、乘/除系数）
    this.data.push(new AttributeValue("生命值", "health", 1, 0)
        .setTooltip('基准值。在 flat 模式下为固定生命值；在 percent 模式下为最大生命百分比；在 multiply/divide 模式下为乘/除系数')
    );

    // 操作动作下拉
    this.data.push(new ListValue("动作", "action", [ "set", "add", "remove" ], "set")
        .setTooltip('生命值操作类型。set：设置为指定值；add：增加指定值；remove：扣除指定值')
    );

    // 数值计算模式下拉
    this.data.push(new ListValue("计算模式", "mode", [ "flat", "percent", "multiply", "divide" ], "flat")
        .setTooltip('数值解析模式。flat：固定值；percent：最大生命值百分比；multiply：当前生命值乘以系数；divide：当前生命值除以系数')
    );
}

extend('MechanicHeldItem', 'Component');
function MechanicHeldItem()
{
    this.super('Held Item', Type.MECHANIC, false);

    this.description = '设置目标玩家的手持物品槽位，尝试设为技能槽位时无效。';

    this.data.push(new AttributeValue("槽位", "slot", 0, 0)
        .setTooltip('要设置的槽位')
    );
}

extend('MechanicImmunity', 'Component');
function MechanicImmunity()
{
    this.super('Immunity', Type.MECHANIC, false);

    this.description = '在限定时间内提供某一伤害来源的免疫。'

    this.data.push(new ListValue('类型', 'type', DAMAGE_TYPES, 'Poison')
        .setTooltip('要免疫的伤害类型')
    );
    this.data.push(new AttributeValue('秒数', 'seconds', 3, 0)
        .setTooltip('免疫持续时间')
    );
    this.data.push(new AttributeValue('倍率', 'multiplier', 0, 0)
        .setTooltip('受到伤害的倍率，设为 0 表示完全免疫。')
    );
}

extend('MechanicInterrupt', 'Component');
function MechanicInterrupt()
{
    this.super('Interrupt', Type.MECHANIC, false);

    this.description = '若目标正在引导，则打断其引导。';
}


extend('MechanicItem', 'Component');
function MechanicItem()
{
    this.super('Item', Type.MECHANIC, false);

    this.description = '给予每个玩家目标由设定定义的物品。';

    this.data.push(new ListValue('材质', 'material', materialList, 'Arrow')
        .setTooltip('给予玩家的物品类型')
    );
    this.data.push(new IntValue('数量', 'amount', 1)
        .setTooltip('给予玩家的物品数量')
    );
    this.data.push(new IntValue('耐久', 'data', 0)
        .setTooltip('给予玩家的物品耐久值')
    );
    this.data.push(new IntValue('数据', 'byte', 0)
        .setTooltip('给予玩家的物品数据值，例如刷怪蛋类型或羊毛颜色')
    );
    this.data.push(new ListValue('自定义', 'custom', [ 'True', 'False' ], 'False')
        .setTooltip('是否给物品设置自定义名称或 Lore')
    );

    this.data.push(new StringValue('名称', 'name', 'Name').requireValue('custom', [ 'True' ])
        .setTooltip('物品名称')
    );
    this.data.push(new StringListValue('Lore', 'lore', []).requireValue('custom', [ 'True' ])
        .setTooltip('物品 Lore 文本（名称下方的说明）')
    );
}
// MechanicItemPacket
extend('MechanicItemPacket', 'Component');
function MechanicItemPacket()
{
    this.super('Item Packet', Type.MECHANIC, false);

    this.description = '设置玩家某个槽位的虚拟物品，并在一定时间移除';

    this.data.push(new StringValue('霓虹闪光 ID', 'neonFlash', '').requireValue('custom', [ 'True' ])
        .setTooltip('NF ID')
    );
    this.data.push(
        new ListValue(
            '槽位', 'slot',
            [ 'HAND', 'OFF_HAND' , 'HELMET', 'CHEST_PLATE', 'LEGGINGS', 'BOOTS'], "HAND"
        ).setTooltip('目标槽位')
    );
    this.data.push(new AttributeValue('秒数', 'delay', 1, 0)
        .setTooltip('移除时间')
    );

    this.data.push(new ListValue('材质', 'material', materialList, '')
        .setTooltip('给予玩家的物品类型')
    );
    this.data.push(new IntValue('数量', 'amount', 1)
        .setTooltip('给予玩家的物品数量')
    );
    this.data.push(new IntValue('耐久', 'data', 0)
        .setTooltip('给予玩家的物品耐久值')
    );
    this.data.push(new IntValue('数据', 'byte', 0)
        .setTooltip('给予玩家的物品数据值，例如刷怪蛋类型或羊毛颜色')
    );
    this.data.push(new StringValue('名称', 'name', 'Name').requireValue('custom', [ 'True' ])
        .setTooltip('物品名称')
    );

}

extend('MechanicItemProjectile', 'Component');
function MechanicItemProjectile()
{
    this.super('Item Projectile', Type.MECHANIC, true);

    this.description = '以物品作为外观发射抛射物，落地后执行子组件。传入的目标为被碰撞的目标，未命中时为落点位置。';


    this.data.push(new ListValue('物品', 'item', materialList, 'Jack O Lantern')
        .setTooltip('作为抛射物的物品类型')
    ),
    this.data.push(new IntValue('物品数据', 'item-data', 0)
        .setTooltip('作为抛射物的物品耐久值，主要用于染料或羊毛等有颜色的物品')
    ),

    addProjectileOptions(this);
    addEffectOptions(this, true);
}

extend('MechanicItemRemove', 'Component');
function MechanicItemRemove()
{
    this.super('Item Remove', Type.MECHANIC, false);

    this.description = '从玩家背包中移除物品，对怪物无效。';

    this.data.push(new AttributeValue('数量', 'amount', 1, 0)
        .setTooltip('玩家背包中需要的物品数量')
    );

    addItemOptions(this);
}

extend('MechanicLaunch', 'Component');
function MechanicLaunch()
{
    this.super('Launch', Type.MECHANIC, false);

    this.description = '按目标的前方方向发射目标。负值表示反方向（例如负的前向速度会让目标向后飞）';

    this.data.push(new ListValue('[高级] 相对', 'relative', [ 'Target', 'Caster', 'Between'], 'Target')
        .setTooltip('决定何为「前方」。Target 使用目标朝向，Caster 使用施法者朝向，Between 使用施法者到目标的方向。')
    );
    this.data.push(new AttributeValue('前向速度', 'forward', 0, 0)
        .setTooltip('给予目标沿其朝向的速度')
    );
    this.data.push(new AttributeValue('上向速度', 'upward', 2, 0.5)
        .setTooltip('给予目标向上的速度')
    );
    this.data.push(new AttributeValue('右向速度', 'right', 0, 0)
        .setTooltip('给予目标向右的速度')
    );
}

extend('MechanicLightning', 'Component');
function MechanicLightning()
{
    this.super('Lightning', Type.MECHANIC, false);

    this.description = '在目标身上或附近召唤闪电。负偏移表示反方向（例如负的前向偏移会落在目标身后）。';

    this.data.push(new ListValue('伤害', 'damage', ['True', 'False'], 'True')
        .setTooltip('闪电是否造成伤害')
    );
    this.data.push(new AttributeValue('前向偏移', 'forward', 0, 0)
        .setTooltip('闪电落在目标前方多少格')
    );
    this.data.push(new AttributeValue('右偏移', 'right', 0, 0)
        .setTooltip('闪电落在目标右侧多少格')
    );
}

extend('MechanicMana', 'Component');
function MechanicMana() {
    this.super('Mana', Type.MECHANIC, false);

    this.description = '恢复或扣除目标的法力。';

    this.data.push(new ListValue('类型', 'type', [ 'Mana', 'Percent' ], 'Mana')
        .setTooltip('恢复或扣除法力的单位。Mana 为固定数值，Percent 为最大法力的百分比。')
    );
    this.data.push(new AttributeValue('数值', 'value', 1, 0)
        .setTooltip('恢复或扣除的法力值')
    );
}

extend('MechanicMessage', 'Component');
function MechanicMessage() {
    this.super('Message', Type.MECHANIC, false);

    this.description = '向每个玩家目标发送消息。如需引用 Value 组件保存的数值，可用 {<key>} 作为过滤器，其中 <key> 是保存时使用的键。'

    this.data.push(new StringValue('消息', 'message', 'text')
        .setTooltip('要显示的消息')
    );
}

extend('MechanicParticle', 'Component');
function MechanicParticle()
{
    this.super('Particle', Type.MECHANIC, false);

    this.description = '在目标附近播放粒子特效。';

    addParticleOptions(this);

    this.data.push(new DoubleValue('前向偏移', 'forward', 0)
        .setTooltip('粒子在目标前方多少格播放，负值表示身后。')
    );
    this.data.push(new DoubleValue('上向偏移', 'upward', 0)
        .setTooltip('粒子在目标上方多少格播放，负值表示下方。')
    );
    this.data.push(new DoubleValue('右偏移', 'right', 0)
        .setTooltip('粒子在目标右侧多少格播放，负值表示左侧。')
    );
}

extend('MechanicParticleAnimation', 'Component');
function MechanicParticleAnimation()
{
    this.super('Particle Animation', Type.MECHANIC, false);

    this.description = '在目标位置随时间播放动画粒子特效，并通过各种变换实现动画。';

    this.data.push(new IntValue('步数', 'steps', 1, 0)
        .setTooltip('每次应用时播放粒子并执行平移的次数')
    );
    this.data.push(new DoubleValue('频率', 'frequency', 0.05, 0)
        .setTooltip('动画应用间隔（秒）。0.05 为最快（1 tick），更低的值效果相同。')
    );
    this.data.push(new IntValue('角度', 'angle', 0)
        .setTooltip('动画在持续时间内旋转的角度')
    );
    this.data.push(new IntValue('起始角度', 'start', 0)
        .setTooltip('动画的初始朝向，水平平移与前向/右向偏移都基于此。')
    );
    this.data.push(new AttributeValue('持续时间', 'duration', 5, 0)
        .setTooltip('动画持续时长（秒）')
    );
    this.data.push(new AttributeValue('水平偏移', 'h-translation', 0, 0)
        .setTooltip('每周期动画相对中心水平移动的距离。正值向外扩张，负值向内收缩。')
    );
    this.data.push(new AttributeValue('垂直偏移', 'v-translation', 0, 0)
        .setTooltip('每周期动画垂直移动的距离。正值上升，负值下沉。')
    );
    this.data.push(new IntValue('水平循环', 'h-cycles', 1)
        .setTooltip('动画过程中水平位置移动的次数，每隔一个周期回到起点。例如两个周期会先移出再移回。')
    );
    this.data.push(new IntValue('垂直循环', 'v-cycles', 1)
        .setTooltip('动画过程中垂直位置移动的次数，每隔一个周期回到起点。例如两个周期会先上升再下降。')
    );

    addParticleOptions(this);

    this.data.push(new DoubleValue('前向偏移', 'forward', 0)
        .setTooltip('粒子在目标前方多少格播放，负值表示身后。')
    );
    this.data.push(new DoubleValue('上向偏移', 'upward', 0)
        .setTooltip('粒子在目标上方多少格播放，负值表示下方。')
    );
    this.data.push(new DoubleValue('右偏移', 'right', 0)
        .setTooltip('粒子在目标右侧多少格播放，负值表示左侧。')
    );
}

extend('MechanicParticleEffect', 'Component');
function MechanicParticleEffect()
{
    this.super('Particle Effect', Type.MECHANIC, false);

    this.description = '播放跟随当前目标的粒子特效，用公式决定形状、大小与运动轨迹';

    addEffectOptions(this, false);
}

extend('MechanicBounceProjectile', 'Component');
function MechanicBounceProjectile()
{
    this.super('Bounce Projectile', Type.MECHANIC, true);

    this.description = '发射具有物理反弹和递归分裂能力的载体抛射物。碰撞和结束均有对应事件，碰撞时执行子组件；也可配合 Bounce Collision / Bounce Split / Bounce End 触发器使用。';

    addProjectileOptions(this);

    this.data.push(new DoubleValue('重力', 'gravity', 0)
        .setTooltip('每 tick 的垂直速度变化量，负值向下，正值向上。')
    );
    this.data.push(new DoubleValue('存活时间', 'lifespan', 3)
        .setTooltip('抛射物最长存在时间，单位为秒。')
    );
    this.data.push(new DoubleValue('碰撞半径', 'radius', 1.5)
        .setTooltip('实体碰撞半径。')
    );
    this.data.push(new ListValue('穿透', 'pierce', [ 'True', 'False' ], 'False')
        .setTooltip('是否穿透实体；同一实体仍只会被同一抛射物命中一次。')
    );

    this.data.push(new IntValue('最大反弹次数', 'max-bounces', 3)
        .setTooltip('初始抛射物允许的最大反弹次数，-1 表示不限制。')
    );
    this.data.push(new DoubleValue('弹性系数', 'restitution', 0.85)
        .setTooltip('碰撞后的法向速度保留比例，范围为 0 到 1。')
    );
    this.data.push(new DoubleValue('切向保留', 'tangent-retention', 0.98)
        .setTooltip('碰撞后的切向速度保留比例，范围为 0 到 1。')
    );
    this.data.push(new StringValue('可反弹方块', 'bounce-blocks', 'all')
        .setTooltip('允许反弹的方块材质，使用英文逗号分隔；填写 all 或 * 表示所有实体方块。')
    );
    this.data.push(new DoubleValue('载体刷新频率', 'carrier-frequency', 0.05)
        .setTooltip('载体位置同步频率，默认 0.05 表示每 tick 更新。')
    );
    this.data.push(new DoubleValue('最大步进距离', 'max-step-distance', 0.25)
        .setTooltip('单次碰撞采样的最大距离。')
    );
    this.data.push(new IntValue('每 tick 最大步数', 'max-steps-per-tick', 128)
        .setTooltip('每 tick 的最大碰撞采样次数。')
    );
    this.data.push(new DoubleValue('碰撞推出距离', 'collision-epsilon', 0.001)
        .setTooltip('反弹后将抛射物推离碰撞面的距离。出射位置会自动校验，不会停留在方块内部，0.05 到 0.2 即可。')
    );

    this.data.push(new ListValue('分裂启用', 'split-enabled', [ 'True', 'False' ], 'False')
        .setTooltip('初始抛射物成功反弹后是否分裂。')
    );
    this.data.push(new IntValue('分裂数量', 'split-count', 2)
        .setTooltip('初始抛射物分裂时额外生成的子抛射物数量。')
    );
    this.data.push(new IntValue('最大分裂代数', 'split-max-depth', 1)
        .setTooltip('最大分裂代数：0 禁止分裂，1 仅初始体分裂，2 允许第一代子体继续分裂。')
    );
    this.data.push(new ListValue('分裂模式', 'split-mode', [ 'Horizontal', 'Flat', 'Cone' ], 'Horizontal')
        .setTooltip('Horizontal 保留父体 Y；Flat 强制子代 Y=0；Cone 使用 3D 圆锥分裂。')
    );
    this.data.push(new DoubleValue('分裂角度', 'split-angle', 35)
        .setTooltip('Horizontal 模式为水平扇形半角；Cone 模式为 3D 圆锥角。范围 0 到 180 度。')
    );
    this.data.push(new DoubleValue('分裂速度继承', 'split-speed-retention', 1)
        .setTooltip('子抛射物继承父抛射物速度的比例，0 表示静止，大于 1 表示加速。')
    );
    this.data.push(new ListValue('父体继续运动', 'split-parent-continue', [ 'True', 'False' ], 'True')
        .setTooltip('分裂后父抛射物是否继续运动；False 表示父体结束，仅保留子体。')
    );
    this.data.push(new ListValue('允许重复分裂', 'split-repeat', [ 'True', 'False' ], 'False')
        .setTooltip('同一抛射物是否在后续反弹中继续分裂；False 表示每个抛射物只在第一次有效碰撞分裂一次。')
    );
    this.data.push(new ListValue('子体分裂启用', 'split-child-enabled', [ 'True', 'False' ], 'False')
        .setTooltip('分裂产生的子抛射物是否允许继续分裂，仍受最大分裂代数限制。')
    );
    this.data.push(new IntValue('子体分裂数量', 'split-child-count', 2)
        .setTooltip('子代及后续代数分裂时生成的数量。')
    );
    this.data.push(new IntValue('子体最大反弹次数', 'split-child-max-bounces', 3)
        .setTooltip('子代及后续代数允许的最大反弹次数，-1 表示不限制。')
    );
    this.data.push(new StringValue('子体载体名称', 'split-child-name', '')
        .setTooltip('子代及后续代数使用的载体名称；留空则继承父体的 name。')
    );
    this.data.push(new StringValue('碰撞技能', 'collision-skill', '')
        .setTooltip('初始抛射物碰撞时通过 SkillCastAPI 调用；发射者为施法者，命中实体为目标，留空不触发。')
    );
    this.data.push(new StringValue('分裂碰撞技能', 'split-collision-skill', '')
        .setTooltip('分裂体碰撞时通过 SkillCastAPI 调用；发射者为施法者，命中实体为目标，留空不触发。')
    );

    this.data.push(new StringValue('载体名称', 'name', 'Bounce Projectile'));
    this.data.push(new StringValue('实体类型', 'entity_type', 'ARMOR_STAND'));
    this.data.push(new ListValue('名称显示', 'name-visible', [ 'True', 'False' ], 'False'));
    this.data.push(new ListValue('小型盔甲架', 'small', [ 'True', 'False' ], 'False'));
    this.data.push(new ListValue('是否可见', 'visible', [ 'True', 'False' ], 'True'));
    this.data.push(new ListValue('标记', 'marker', [ 'True', 'False' ], 'False'));
    this.data.push(new StringValue('萌芽特效', 'germ_effect', ''));
    this.data.push(new StringValue('物品名称', 'item_name', 'Bounce Projectile'));
    this.data.push(new ListValue('物品种类', 'item_type', materialList, 'AIR'));

    addEffectOptions(this, true);
}

extend('MechanicParticleProjectile', 'Component');
function MechanicParticleProjectile()
{
    this.super('Particle Projectile', Type.MECHANIC, true);

    this.description = '使用粒子作为其视觉效果发射射弹，在着陆时应用子组件。传递的目标将是碰撞的目标，或者如果没有击中，则是它降落的位置。';

    addProjectileOptions(this);

    this.data.push(new DoubleValue('重力', 'gravity', 0)
        .setTooltip('每个 tick 施加的重力，负值下落，正值上升')
    );
    this.data.push(new ListValue('穿透', 'pierce', [ 'True', 'False' ], 'False')
        .setTooltip('该抛射物是否穿透初始目标并继续命中其后方目标')
    );

    addParticleOptions(this);

    this.data.push(new DoubleValue('频率', 'frequency', 0.05)
        .setTooltip('抛射物所在位置播放粒子特效的频率。除非粒子过多，否则不建议修改。')
    );
    this.data.push(new DoubleValue('存活时间', 'lifespan', 3)
        .setTooltip('抛射物未命中任何目标时的存活时间（秒）')
    );
    this.data.push(new DoubleValue('半径', 'radius', 0.0)
        .setTooltip('碰撞半径')
    );

    this.data.push(new ListValue('是否使用发包盔甲架', 'armor-stand', ['True', 'False'], 'False'));
    this.data.push(new StringValue('盔甲架名称', 'name', 'Armor Stand'));
    this.data.push(new ListValue('名称显示', 'name-visible', ['True', 'False'], 'False'));
    this.data.push(new ListValue('小型盔甲架', 'small', ['True', 'False'], 'True'));
    this.data.push(new ListValue('是否可见', 'visible', ['True', 'False'], 'True'));
    this.data.push(new ListValue('标记', 'marker', ['True', 'False'], 'True'));
    this.data.push(new StringValue('萌芽特效', 'germ_effect', ''));
    this.data.push(new StringValue('物品名称', 'item_name', 'Armor Stand'));
    this.data.push(new ListValue('物品种类', 'item_type', materialList, 'AIR'));

    addEffectOptions(this, true);
}

extend('MechanicEntitySpread', 'Component');
function MechanicEntitySpread()
{
    this.super('Entity Spread', Type.MECHANIC, false);

    this.description = '将施法者扔向目标';

    this.data.push(new AttributeValue('步进', 'step', 0.5, 0.0)
        .setTooltip("每次 tick 的插值"));
    this.data.push(new AttributeValue('重力', 'gravity', 0, 0.0)
        .setTooltip("重力值"));
    this.data.push(new AttributeValue('角度', 'angle', 0, 0.0)
        .setTooltip("头部选择"));
    this.data.push(new AttributeValue('速度', 'speed', 0.05, 0.0)
        .setTooltip("移动速度"));

    this.data.push(new ListValue('跟踪', 'trace', ['True', 'False'], 'False'));
    this.data.push(new ListValue('锁定', 'lock', ['True', 'False'], 'True')
        .setTooltip("锁定后，释放技能是，改玩家不可动，如果命中对方也不可动"));

    this.data.push(new AttributeValue('前向偏移', 'forward', 0,0.0));

    this.data.push(new AttributeValue('上向偏移', 'upward', 0,0.0));

    this.data.push(new AttributeValue('右偏移', 'right', 0,0.0));

    this.data.push(new AttributeValue('左偏移', 'left', 0,0.0));


}

extend('MechanicPassive', 'Component');
function MechanicPassive()
{
    this.super('Passive', Type.MECHANIC, true);

    this.description = '每隔一个周期持续执行子组件，下方秒数即为周期或执行频率。';

    this.data.push(new AttributeValue('秒数', 'seconds', 1, 0)
        .setTooltip('每次执行之间的间隔（秒）')
    );
}

extend('MechanicPermission', 'Component');
function MechanicPermission()
{
    this.super('Permission', Type.MECHANIC, true);

    this.description = '在限定时间内给予每个玩家目标一项权限，需要 Vault 及配套权限插件。';

    this.data.push(new StringValue('权限', 'perm', 'plugin.perm.key')
        .setTooltip('给予玩家的权限节点')
    );
    this.data.push(new AttributeValue('秒数', 'seconds', 3, 0)
        .setTooltip('权限持续时间（秒）')
    );
}

extend('MechanicPotion', 'Component');
function MechanicPotion()
{
    this.super('Potion', Type.MECHANIC, false);

    this.description = '为目标施加一段时间的药水效果。';

    this.data.push(new ListValue('药水', 'potion', [ 'Absorption', 'Blindness', 'Confusion', 'Damage Resistance', 'Fast Digging', 'Fire Resistance', 'Glowing', 'Health Boost', 'Hunger', 'Increase Damage', 'Invisibility', 'Jump', 'Levitation', 'Luck', 'Night Vision', 'Poison', 'Regeneration', 'Saturation', 'Slow', 'Slow Digging', 'Speed', 'Unluck', 'Water Breathing', 'Weakness', 'Wither' ], 'Absorption')
        .setTooltip('要施加的药水效果类型')
    );
    this.data.push(new ListValue('环境粒子', 'ambient', [ 'True', 'False' ], 'True')
        .setTooltip('是否显示环境粒子')
    );
    this.data.push(new AttributeValue('阶级', 'tier', 1, 0)
        .setTooltip('药水效果强度')
    );
    this.data.push(new AttributeValue('秒数', 'seconds', 3, 1)
        .setTooltip('效果持续时间')
    );
}

extend('MechanicPotionProjectile', 'Component');
function MechanicPotionProjectile()
{
    this.super('Potion Projectile', Type.MECHANIC, true);

    this.description = '从每个目标处投掷一瓶不产生药水效果的喷溅药水，落地后执行子元素。传入的目标为被药水命中的所有实体，未命中任何实体时为落点位置。';

    this.data.push(new ListValue('类型', 'type', [ 'Fire Resistance', 'Instant Damage', 'Instant Heal', 'Invisibility', 'Night Vision', 'Poison', 'Regen', 'Slowness', 'Speed', 'Strength', 'Water', 'Water Breathing', 'Weakness' ], 'Fire Resistance')
        .setTooltip('药水外观使用的类型')
    );
    this.data.push(new ListValue("分组", "group", ["Ally", "Enemy", "Both"], "Enemy")
        .setTooltip('命中实体的筛选方式')
    );
    this.data.push(new ListValue('滞留', 'linger', [ 'True', 'False' ], 'False')
        .setTooltip('是否使用滞留药水（仅 1.9+）')
    );
}

extend('MechanicProjectile', 'Component');
function MechanicProjectile()
{
    this.super('Projectile', Type.MECHANIC, true);

    this.description = '发射命中后执行子组件的抛射物，传入的目标为被命中的目标。';

    this.data.push(new ListValue('抛射物', 'projectile', [ 'Arrow', 'Egg', 'Ghast Fireball', 'Snowball' ], 'Arrow')
        .setTooltip('发射的抛射物类型')
    );
    this.data.push(new ListValue('火焰附加', 'flaming', [ 'True', 'False' ], 'False')
        .setTooltip('发射的抛射物是否着火。')
    );
    this.data.push(new ListValue('消耗', 'cost', [ 'None', 'All', 'One' ], 'None')
        .setTooltip('发射物品的技能消耗。All 表示按施放技能消耗相同数量的物品。')
    );

    addProjectileOptions(this);
    addEffectOptions(this, true);
}

extend('MechanicPurge', 'Component');
function MechanicPurge()
{
    this.super('Purge', Type.MECHANIC, false);

    this.description = '清除目标身上的正面药水效果或状态';

    this.data.push(new ListValue('药水', 'potion', [ 'None', 'All', 'Absorption', 'Damage Resistance', 'Fast Digging', 'Fire Resistance', 'Health Boost', 'Increase Damage', 'Invisibility', 'Jump', 'Night Vision', 'Regeneration', 'Saturation', 'Speed', 'Water Breathing' ], 'All')
        .setTooltip('要从目标身上移除的药水效果（如有）')
    );
    this.data.push(new ListValue('状态', 'status', [ 'None', 'All', 'Absorb', 'Invincible' ], 'All')
        .setTooltip('要从目标身上移除的状态（如有）')
    );
}

extend('MechanicPush', 'Component');
function MechanicPush()
{
    this.super('Push', Type.MECHANIC, false);

    this.description = '相对施法者推动目标。以施法者自身为目标时无效。正值产生击退，负值将其拉近。';

  this.data.push(new ListValue('类型', 'type', [ 'Fixed', 'Inverse', 'Scaled' ], 'Fixed')
    .setTooltip('按相对位置调整速度的方式。Fixed 对所有目标使用相同速度，Inverse 让远处敌人被推得更快，Scaled 让近处敌人被推得更快。')
  );
    this.data.push(new AttributeValue('速度', 'speed', 3, 1)
      .setTooltip('推离目标的速度，负值表示拉近。')
  );
    this.data.push(new StringValue('来源', 'source', 'none')
        .setTooltip('推拉的作用源，应为「记住目标」组件中使用的键；未记住任何目标时默认为施法者。')
    );
}

extend('MechanicRememberTargets', 'Component');
function MechanicRememberTargets()
{
    this.super('Remember Targets', Type.MECHANIC, false);

    this.description = '把当前目标保存到指定键下，供后续使用';

    this.data.push(new StringValue('键名', 'key', 'target')
        .setTooltip('保存目标使用的唯一键，「记住目标」将用它来对目标施加后续效果。')
    );
}

extend('MechanicRepeat', 'Component');
function MechanicRepeat()
{
    this.super('Repeat', Type.MECHANIC, true);

    this.description = '多次执行子组件。执行时机由延迟（首次执行前的秒数）与周期（两次执行间的秒数）决定。';

    this.data.push(new AttributeValue('重复次数', 'repetitions', 3, 0)
        .setTooltip('激活子组件的次数')
    );
    this.data.push(new DoubleValue('周期', 'period', 1)
        .setTooltip('每次执行子组件之间的秒数')
    );
    this.data.push(new DoubleValue('延迟', 'delay', 0)
        .setTooltip('开始执行子组件前的初始延迟')
    );
    this.data.push(new ListValue('失败时停止', 'stop-on-fail', [ 'True', 'False' ], 'False')
        .setTooltip('效果失败时是否提前停止重复任务')
    );
}

extend('MechanicSound', 'Component');
function MechanicSound()
{
    this.super('Sound', Type.MECHANIC, false);

    this.description = "播放音效，支持萌芽";

    this.data.push(new ListValue('发包音效', 'GermSound', [ 'true', 'false' ], 'true')
    );

    this.data.push(new StringValue('音效', 'sound', 'Ambience Cave').requireValue('GermSound', [ 'true' ])
        .setTooltip('要播放的音效')
    );
    this.data.push(new ListValue('音效', 'sound', SOUNDS_PRE, 'Ambience Cave').requireValue('GermSound', [ 'false' ])
        .setTooltip('要播放的音效')
    );

    this.data.push(new AttributeValue('音量', 'volume', 100, 0)
        .setTooltip('音量百分比。超过 100 不会更响，但传播距离更远')
    );
    this.data.push(new AttributeValue('俯仰角', 'pitch', 1, 0)
        .setTooltip('音调，即 0.5 到 2 之间的速度倍率。')
    );
}

extend('MechanicSpeed', 'Component');
function MechanicSpeed()
{
    this.super('Speed', Type.MECHANIC, false);

    this.description = '以倍率修改玩家的基础速度（与药水叠加）';

    this.data.push(new AttributeValue('倍率', 'multiplier', 1.2, 0)
        .setTooltip('玩家基础速度的倍率')
    );
    this.data.push(new AttributeValue('持续时间', 'duration', 3, 1)
        .setTooltip('速度加成持续时间')
    );
}

extend('MechanicStatus', 'Component');
function MechanicStatus()
{
    this.super('Status', Type.MECHANIC, false);

    this.description = '为目标施加一段时间的状态效果。';

    this.data.push(new ListValue('状态', 'status', [ 'Absorb', 'Curse', 'Disarm', 'Invincible', 'Root', 'Silence', 'Stun' ], 'Stun')
        .setTooltip('要施加的状态')
    );
    this.data.push(new AttributeValue('持续时间', 'duration', 3, 1)
        .setTooltip('状态持续时间（秒）')
    );
}

extend('MechanicTaunt', 'Component');
function MechanicTaunt()
{
    this.super('Taunt', Type.MECHANIC, false);

    this.description = '吸引目标生物的仇恨。普通怪物会转而攻击施法者。旧版本服务端的 Spigot/Bukkit 接口不可用，可能无法生效。对 MythicMobs 使用其仇恨系统，数值由下方设置决定。';

    this.data.push(new AttributeValue('数量', 'amount', 1, 0)
        .setTooltip('MythicMobs 生效时的仇恨值，负值表示降低仇恨')
    );
}

extend('MechanicTrigger', 'Component');
function MechanicTrigger()
{
    this.super('Trigger', Type.MECHANIC, true);

    this.description = '在一段时间内监听当前目标身上的触发器。';

    this.data.push(new ListValue('触发器', 'trigger', [ 'Crouch', 'Death', 'Environment Damage', 'Kill', 'Land', 'Launch', 'Physical Damage', 'Skill Damage', 'Took Physical Damage', 'Took Skill Damage' ], 'Death')
        .setTooltip('要监听的触发器')
    );
    this.data.push(new AttributeValue('持续时间', 'duration', 5, 0)
        .setTooltip('监听持续时间')
    );
    this.data.push(new ListValue('可堆叠', 'stackable', [ 'True', 'False', ], 'True')
        .setTooltip('不同玩家（或同一玩家）是否可以同时监听同一目标')
    );
    this.data.push(new ListValue('仅一次', 'once', [ 'True', 'False' ], 'True')
        .setTooltip('该触发器每次施法是否只使用一次。为 false 时，在持续时间内可多次执行。')
    );

    // CROUCH
    this.data.push(new ListValue('类型', 'type', [ 'Start Crouching', 'Stop Crouching', 'Both' ], 'Start Crouching')
        .requireValue('trigger', [ 'Crouch' ])
        .setTooltip('是否只在潜行（或非潜行）时执行子组件')
    );

    // ENVIRONMENT_DAMAGE
    this.data.push(new ListValue('类型', 'type', DAMAGE_TYPES, 'FALL')
        .requireValue('trigger', [ 'Environment Damage' ])
        .setTooltip('要响应的伤害来源')
    );

    // LAND
    this.data.push(new DoubleValue('最小距离', 'min-distance', 0)
        .requireValue('trigger', [ 'Land' ])
        .setTooltip('触发前玩家需要下落的最小距离。')
    );

    // LAUNCH
    this.data.push(new ListValue('类型', 'type', [ 'Any', 'Arrow', 'Egg', 'Ender Pearl', 'Fireball', 'Fishing Hook', 'Snowball' ], 'Any')
        .requireValue('trigger', [ 'Launch' ])
        .setTooltip('需要发射的抛射物类型。')
    );

    // PHYSICAL
    this.data.push(new ListValue('类型', 'type', [ 'Both', 'Melee', 'Projectile' ], 'Both')
        .requireValue('trigger', [ 'Physical Damage', 'Took Physical Damage' ])
        .setTooltip('造成的伤害类型')
    );

    // SKILL
    this.data.push(new StringValue('分类', 'category', '')
        .requireValue('trigger', [ 'Skill Damage', 'Took Skill Damage' ])
        .setTooltip('要响应的技能伤害类型，留空表示所有技能伤害。')
    );

    // DAMAGE
    var damageTriggers = [ 'Physical Damage', 'Skill Damage', 'Took Physical Damage', 'Took Skill Damage' ];
    this.data.push(new ListValue('监听目标', 'target', [ 'True', 'False' ], 'True')
        .requireValue('trigger', damageTriggers)
        .setTooltip('True 时子组件以被监听的目标为目标，False 时以与目标战斗的实体为目标。')
    );
    this.data.push(new DoubleValue("最小伤害", "dmg-min", 0)
        .requireValue('trigger', damageTriggers)
        .setTooltip('需要造成的最小伤害')
    );
    this.data.push(new DoubleValue("最大伤害", "dmg-max", 999)
        .requireValue('trigger', damageTriggers)
        .setTooltip('需要造成的最大伤害')
    );
}

extend('MechanicDataEdit', 'Component');
function MechanicDataEdit()
{
    this.super('Data Edit', Type.MECHANIC, false);

    this.data.push(new StringValue('键名', 'key', 'key')
    );
    this.data.push(new StringValue('数值', 'value', 'value')
    );
    this.data.push(new ListValue('动作', 'action', [ '+', '-', '*', '/'], '+')
    );
}

extend('MechanicValueAdd', 'Component');
function MechanicValueAdd()
{
    this.super('Value Add', Type.MECHANIC, false);
    
    this.description = '在施法者的唯一键下累加存储值。若该值此前未设置，则直接设为给定数值。';
    
    this.data.push(new StringValue('键名', 'key', 'value')
        .setTooltip('保存数值时使用的唯一键，可在属性值位置引用该键来使用存储值。')
    );
    this.data.push(new AttributeValue('数量', 'amount', 1, 0)
        .setTooltip('累加的数值')
    );
}

extend('MechanicValueAttribute', 'Component');
function MechanicValueAttribute() 
{
    this.super('Value Attribute', Type.MECHANIC, false);
    
    this.description = '把玩家某项属性的数值读取为存储值，供其它组件使用。';
    
    this.data.push(new StringValue('键名', 'key', 'attribute')
        .setTooltip('保存数值时使用的唯一键，可在属性值位置引用该键来使用存储值。')
    );
    this.data.push(new StringValue('属性', 'attribute', 'Vitality')
        .setTooltip('要读取的属性名称')
    );
}

extend('MechanicValueCopy', 'Component');
function MechanicValueCopy()
{
    this.super('Value Copy', Type.MECHANIC, false);
    
    this.description = '在施法者与目标之间复制存储值';
    
    this.data.push(new StringValue('键名', 'key', 'value')
        .setTooltip('保存数值时使用的唯一键，可在属性值位置引用该键来使用存储值。')
    );
    this.data.push(new StringValue('目标位置', 'destination', 'value')
        .setTooltip('要复制到的目标键')
    );
    this.data.push(new ListValue('面向目标', 'to-target', [ 'True', 'False' ], 'True')
        .setTooltip('累加的数值')
    );
}

extend('MechanicValueDistance', 'Component');
function MechanicValueDistance()
{
    this.super('Value Distance', Type.MECHANIC, false);

    this.description = '把目标与施法者之间的距离保存为数值';

    this.data.push(new StringValue('键名', 'key', 'attribute')
        .setTooltip('保存数值时使用的唯一键，可在属性值位置引用该键来使用存储值。')
    );
}

extend('MechanicValueHealth', 'Component');
function MechanicValueHealth()
{
    this.super('Value Health', Type.MECHANIC, false);
    
    this.description = '把目标当前生命值保存到施法者的指定键下';
    
    this.data.push(new StringValue('键名', 'key', 'value')
        .setTooltip('保存数值时使用的唯一键，可在属性值位置引用该键来使用存储值。')
    );
    this.data.push(new ListValue('类型', 'type', [ 'Current', 'Max', 'Missing', 'Percent' ], 'Current')
        .setTooltip('Current 为当前生命值，max 为最大生命值，missing 为已损失生命值，percent 为生命值占最大值的比例。')
    );
}

extend('MechanicValueLocation', 'Component');
function MechanicValueLocation() 
{
    this.super('Value Location', Type.MECHANIC, false);
    
    this.description = '把第一个目标的当前位置保存为存储值，供后续使用。';
    
    this.data.push(new StringValue('键名', 'key', 'location')
        .setTooltip('保存位置时使用的唯一键，可在属性值位置引用该键来使用存储值。')
    );
}

extend('MechanicValueLore', 'Component');
function MechanicValueLore()
{
    this.super('Value Lore', Type.MECHANIC, false);
    
    this.description = '从手持物品的 Lore 中读取数值，保存到施法者的指定唯一键下。';
    
    this.data.push(new StringValue('键名', 'key', 'lore')
        .setTooltip('保存数值时使用的唯一键，可在属性值位置引用该键来使用存储值。')
    );
    this.data.push(new ListValue("手", "hand", [ 'Main', 'Offhand' ], 'Main')
        .setTooltip('检查物品的手，副手仅 1.9+ 版本可用。')
    );
    this.data.push(new StringValue('正则', 'regex', 'Damage: {value}')
        .setTooltip('要查找的正则字符串，用 {value} 标记要保存的数值。不熟悉正则可在网上查阅，或避免使用 [ ] { } ( ) . + ? * ^ \\ | 等特殊字符')
    );
    this.data.push(new AttributeValue('倍率', 'multiplier', 1, 0)
        .setTooltip('读取数值的倍率。若要保持原值，设为 1。')
    );
}

extend('MechanicValueLoreSlot', 'Component');
function MechanicValueLoreSlot()
{
    this.super('Value Lore Slot', Type.MECHANIC, false);
    
    this.description = '从物品 Lore 中读取数值，保存到施法者的指定唯一键下。';
    
    this.data.push(new StringValue('键名', 'key', 'lore')
        .setTooltip('保存数值时使用的唯一键，可在属性值位置引用该键来使用存储值。')
    );
    this.data.push(new IntValue("槽位", "slot", 9)
        .setTooltip('读取物品的背包槽位。0-8 为快捷栏，9-35 为背包，36-39 为护甲，40 为副手。')
    );
    this.data.push(new StringValue('正则', 'regex', 'Damage: {value}')
        .setTooltip('要查找的正则字符串，用 {value} 标记要保存的数值。不熟悉正则可在网上查阅，或避免使用 [ ] { } ( ) . + ? * ^ \\ | 等特殊字符')
    );
    this.data.push(new AttributeValue('倍率', 'multiplier', 1, 0)
        .setTooltip('读取数值的倍率。若要保持原值，设为 1。')
    );
}

extend('MechanicValueMana', 'Component');
function MechanicValueMana()
{
    this.super('Value Mana', Type.MECHANIC, false);
    
    this.description = '把目标玩家当前法力保存到施法者的指定键下';
    
    this.data.push(new StringValue('键名', 'key', 'value')
        .setTooltip('保存数值时使用的唯一键，可在属性值位置引用该键来使用存储值。')
    );
    this.data.push(new ListValue('类型', 'type', [ 'Current', 'Max', 'Missing', 'Percent' ], 'Current')
        .setTooltip('Current 为当前法力，max 为最大法力，missing 为已损失法力，percent 为法力占最大值的比例。')
    );
}

extend('MechanicValueMultiply', 'Component');
function MechanicValueMultiply()
{
    this.super('Value Multiply', Type.MECHANIC, false);

    this.description = '乘以施法者唯一键下的存储值。若该值此前未设置则不起作用。';

    this.data.push(new StringValue('键名', 'key', 'value')
        .setTooltip('保存数值时使用的唯一键，可在属性值位置引用该键来使用存储值。')
    );
    this.data.push(new AttributeValue('倍率', 'multiplier', 1, 0)
        .setTooltip('数值的乘数')
    );
}

extend('MechanicValuePlaceholder', 'Component');
function MechanicValuePlaceholder()
{
    this.super('Value Placeholder', Type.MECHANIC, false);

    this.description = '使用占位符字符串并把结果保存为施法者的数值';

    this.data.push(new StringValue('键名', 'key', 'value')
        .setTooltip('保存数值时使用的唯一键，可在属性值位置引用该键来使用存储值。')
    );
    this.data.push(new ListValue("类型", "type", [ 'Number', 'String' ], 'Number')
        .setTooltip('保存的类型。Number 需要数值型占位符，String 可用于消息或指令。')
    );
    this.data.push(new StringValue('占位符', 'placeholder', '%player_food_level%')
        .setTooltip('要使用的占位符字符串。选择 String 类型时可包含多个占位符。')
    );
}

extend('MechanicValueNeonFlashAttr', 'Component');
function MechanicValueNeonFlashAttr()
{
    this.super('Value NeonFlash Attr', Type.MECHANIC, false);

    this.description = '取指定槽位装备的NeonFlash物品属性';

    this.data.push(new StringValue('键名', 'key', 'value')
        .setTooltip('储存的KEY，取得数据后储存')
    );
    this.data.push(new StringValue('NBT 数据', 'nbt', "ATTACK_DAMAGE")
        .setTooltip('要取的data键')
    );
    this.data.push(
        new ListValue(
            '槽位', 'slot',
            [ 'HAND', 'OFF_HAND' , 'HELMET', 'CHEST_PLATE', 'LEGGINGS', 'BOOTS'], "HAND"
        ).setTooltip('要取的装备槽位')
    );
}

extend('MechanicValueRandom', 'Component')
function MechanicValueRandom()
{
    this.super('Value Random', Type.MECHANIC, false);
    
    this.description = '把指定数值保存到施法者的指定键下。';
    
    this.data.push(new StringValue('键名', 'key', 'value')
        .setTooltip('保存数值时使用的唯一键，可在属性值位置引用该键来使用存储值。')
    );
    this.data.push(new ListValue('类型', 'type', [ 'Normal', 'Triangular' ], 'Normal')
        .setTooltip('随机方式。Triangular 偏向中间值，类似掷两颗骰子。')
    );
    this.data.push(new AttributeValue('最小', 'min', 0, 0)
        .setTooltip('可取的最小值')
    );
    this.data.push(new AttributeValue('最大', 'max', 0, 0)
        .setTooltip('可取的最大值')
    );
}

extend('MechanicValueSet', 'Component');
function MechanicValueSet()
{
    this.super('Value Set', Type.MECHANIC, false);
    
    this.description = '把指定数值保存到施法者的指定键下。';
    
    this.data.push(new StringValue('键名', 'key', 'value')
        .setTooltip('保存数值时使用的唯一键，可在属性值位置引用该键来使用存储值。')
    );
    this.data.push(new AttributeValue('数值', 'value', 1, 0)
        .setTooltip('要保存到该键的数值')
    );
}

extend('MechanicWarp', 'Component');
function MechanicWarp()
{
    this.super('Warp', Type.MECHANIC, false);
    
    this.description = '按目标的前方方向传送目标。负值表示反方向（例如负的前向距离会让目标向后传送）。';
    
    this.data.push(new ListValue('穿墙', 'walls', [ 'True', 'False' ], 'False')
        .setTooltip('是否允许目标穿墙传送')
    );
    this.data.push(new AttributeValue('前向', 'forward', 3, 1)
        .setTooltip('向前传送的格数，负值表示向后。')
    );
    this.data.push(new AttributeValue('上向', 'upward', 0, 0)
        .setTooltip('向上传送的格数，负值表示向下。')
    );
    this.data.push(new AttributeValue('右向', 'right', 0, 0)
        .setTooltip('向右传送的格数，负值表示向左。')
    );
}

extend('MechanicWarpLoc', 'Component');
function MechanicWarpLoc()
{
    this.super('Warp Location', Type.MECHANIC, false);
    
    this.description = '把目标传送到指定位置。';
    
    this.data.push(new StringValue('世界（或 "current"）', 'world', 'current')
        .setTooltip('该位置所在世界名称')
    );
    this.data.push(new DoubleValue('X', 'x', 0)
        .setTooltip('目标位置的 X 坐标')
    );
    this.data.push(new DoubleValue('Y', 'y', 0)
        .setTooltip('目标位置的 Y 坐标')
    );
    this.data.push(new DoubleValue('Z', 'z', 0)
        .setTooltip('目标位置的 Z 坐标')
    );
    this.data.push(new DoubleValue('偏航角', 'yaw', 0)
        .setTooltip('目标位置的偏航角（左右朝向）')
    );
    this.data.push(new DoubleValue('俯仰角', 'pitch', 0)
        .setTooltip('目标位置的俯仰角（上下朝向）')
    );
}

extend('MechanicWarpRandom', 'Component');
function MechanicWarpRandom()
{
    this.super('Warp Random', Type.MECHANIC, false);
    
    this.description = '把目标朝随机方向传送指定距离。';
    
    this.data.push(new ListValue('仅水平', 'horizontal', [ 'True', 'False' ], 'True')
        .setTooltip('是否把随机位置限制在水平面上')
    );
    this.data.push(new ListValue('穿墙', 'walls', [ 'True', 'False' ], 'False')
        .setTooltip('是否允许目标穿墙传送')
    );
    this.data.push(new AttributeValue('距离', 'distance', 3, 1)
        .setTooltip('最大传送距离（格）')
    );
}

extend('MechanicWarpSwap', 'Component');
function MechanicWarpSwap()
{
    this.super('Warp Swap', Type.MECHANIC, false);
    
    this.description = '交换施法者与目标的位置。若有多个目标，只取第一个。';
}

extend('MechanicWarpTarget', 'Component');
function MechanicWarpTarget()
{
    this.super('Warp Target', Type.MECHANIC, false);
    
    this.description = '把目标或施法者传送到另一方身边。目标为施法者自身时无效。';

    this.data.push(new AttributeValue('前向', 'forward', 0, 0)
        .setTooltip('沿目标朝向的偏移距离，负数表示向后。')
    );
    this.data.push(new AttributeValue('上向', 'upward', 2, 0.5)
        .setTooltip('相对目标向上的偏移距离，负数表示向下。')
    );
    this.data.push(new AttributeValue('右向', 'right', 0, 0)
        .setTooltip('相对目标向右的偏移距离，负数表示向左。')
    );

    this.data.push(new ListValue('类型', 'type', [ 'Caster to Target', 'Target to Caster' ], 'Caster to Target')
        .setTooltip('传送相关目标的方向')
    );
}

extend('MechanicWarpValue', 'Component');
function MechanicWarpValue() 
{
    this.super('Warp Value', Type.MECHANIC, false);
    
    this.description = '把所有目标传送到由 Value Location 组件记住的位置。';
    
    this.data.push(new StringValue('键名', 'key', 'location')
        .setTooltip('保存位置时使用的唯一键，需与 Value Location 组件中使用的键一致。')
    );
}

extend('MechanicWolf', 'Component');
function MechanicWolf() {
    this.super('Wolf', Type.MECHANIC, true);
    
    this.description = '为每个目标召唤一只限时狼。子组件初始以该狼为目标，可为其添加效果；也可赋予独立技能，但 Cast 触发器不会触发。';
    
    this.data.push(new ListValue('项圈颜色', 'color', dyeList, 'Black')
        .setTooltip('狼项圈的颜色')
    );
    this.data.push(new StringValue('狼名称', 'name', "{player}'s Wolf")
        .setTooltip('狼的显示名称，可用 {player} 插入施法者名称。')
    );
    this.data.push(new AttributeValue('生命值', 'health', 10, 0)
        .setTooltip('狼的初始生命值')
    );
    this.data.push(new AttributeValue('伤害', 'damage', 3, 0)
        .setTooltip('狼每次攻击造成的伤害')
    );
    this.data.push(new ListValue('坐下', 'sitting', [ 'True', 'False' ], 'False')
        .setTooltip('[高级] 狼召唤时是否处于坐下状态')
    );
    this.data.push(new AttributeValue('持续时间', 'seconds', 10, 0)
        .setTooltip('狼的持续时间')
    );
    this.data.push(new AttributeValue('数量', 'amount', 1, 0)
        .setTooltip('召唤的狼数量')
    );
    this.data.push(new StringListValue('技能（每行一个）', 'skills', [])
        .setTooltip('赋予狼的技能。技能按召唤它的技能等级执行；需要 Cast 触发器的技能不会生效。')
    );
}

extend('MechanicArmorStand', 'Component');
function MechanicArmorStand() {
    this.super('Armor Stand', Type.MECHANIC, true);

    this.description = '召唤实体盔甲架';

    this.data.push(new AttributeValue('删除时间', 'duration', 5, 0)
    );
    this.data.push(new StringValue('盔甲架名称', 'name', 'Armor Stand')
    );
    this.data.push(new ListValue('名称显示', 'name-visible', ['True', 'False'], 'False')
    );
    this.data.push(new ListValue('跟随目标', 'follow', ['True', 'False'], 'False')
    );
    this.data.push(new ListValue('重力', 'gravity', ['True', 'False'], 'True')
    );
    this.data.push(new ListValue('小型盔甲架', 'tiny', ['True', 'False'], 'False')
    );
    this.data.push(new ListValue('显示盔甲', 'arms', ['True', 'False'], 'False')
    );
    this.data.push(new ListValue('显示底板', 'base', ['True', 'False'], 'False')
    );
    this.data.push(new ListValue('是否可见', 'visible', ['True', 'False'], 'True')
    );
    this.data.push(new ListValue('标记', 'marker', ['True', 'False'], 'True')
        .setTooltip('会移除盔甲架碰撞')
    );
    // randomDir
    this.data.push(new ListValue('随机朝向', 'randomDir', ['True', 'False'], 'False')
        .setTooltip('随机朝向')
    );
    this.data.push(new AttributeValue('前偏移', 'forward', 0, 0)
    );
    this.data.push(new AttributeValue('上偏移', 'upward', 0, 0)
    );
    this.data.push(new AttributeValue('右偏移', 'right', 0, 0)
    );
    this.data.push(new StringValue('物品名称', 'item_name', 'Armor Stand'));
    this.data.push(new ListValue('物品种类', 'item_type', materialList, 'Arrow'));

}

extend('MechanicMythicSummon', 'Component');
function MechanicMythicSummon() {
    this.super('Mythic Summon', Type.MECHANIC, true);

    this.description = '召唤MM实体，并关联召唤者';
    this.data.push(new StringValue('怪物内部ID', 'type', 'Husk'));
    this.data.push(new ListValue('是否使用内置AI', 'useAi', ['True', 'False'], 'False')
        .setTooltip('如果设置使用，需要把MM怪物的AI全部清除，内置AI会主动寻敌'));
    this.data.push(new AttributeValue('继承生命值比例', 'health', 0.5, 0));
    this.data.push(new AttributeValue('删除时间', 'duration', 5, 0));
    this.data.push(new AttributeValue('前偏移', 'forward', 0, 0));
    this.data.push(new AttributeValue('上偏移', 'upward', 0, 0));
    this.data.push(new AttributeValue('右偏移', 'right', 0, 0));

}
// MechanicMythicFaction
extend('MechanicMythicFaction', 'Component');
function MechanicMythicFaction() {
    this.super('Mythic Faction', Type.MECHANIC, true);

    this.description = '临时切换MM怪物派系，前提是SK所选的目标是MM怪物';
    this.data.push(new StringValue('派系名称', 'faction', 'default')
    );
    this.data.push(new AttributeValue('恢复时间', 'duration', 5, 0)
    );

}

extend('MechanicAdyeshach', 'Component');
function MechanicAdyeshach() {
    this.super('Adyeshach', Type.MECHANIC, true);

    this.description = '召唤Adyeshach虚拟实体';

    this.data.push(new AttributeValue('删除时间', 'duration', 5, 0)
    );
    this.data.push(new StringValue('名称', 'name', 'Adyeshach')
    );
    this.data.push(new ListValue('名称显示', 'name-visible', ['True', 'False'], 'False')
    );
    this.data.push(new StringValue('生物种类', 'type', 'PLAYER'));
    this.data.push(new StringValue('皮肤', 'texture', ''));
    this.data.push(new ListValue('重力', 'gravity', ['True', 'False'], 'True')
    );
    this.data.push(new ListValue('是否可见', 'visible', ['True', 'False'], 'True')
    );
    this.data.push(new AttributeValue('前偏移', 'forward', 0, 0)
    );
    this.data.push(new AttributeValue('上偏移', 'upward', 0, 0)
    );
    this.data.push(new AttributeValue('右偏移', 'right', 0, 0)
    );
    this.data.push(new ListValue('是否头戴物品', 'use_item',  ['True', 'False'], 'False'));
    this.data.push(new StringValue('物品名称', 'item_name', 'Armor Stand'));
    this.data.push(new ListValue('物品种类', 'item_type', materialList, 'Arrow'));

}

extend('MechanicArmorStandRemove', 'Component');
function MechanicArmorStandRemove() {
    this.super('Armor Stand Remove', Type.MECHANIC, true);

    this.description = '删除盔甲架';

    this.data.push(new ListValue('移除目标', 'target', ['True', 'False'], 'True')
        .setTooltip('是否是移除目标的盔甲架，而不是施法者'));
}

extend('MechanicGermAnimationStart', 'Component');
function MechanicGermAnimationStart() {
    this.super('Germ Animation Start', Type.MECHANIC, true);

    this.description = '开始播放萌芽动作';

    this.data.push(new StringValue('动画名称', 'name', 'default')
        .setTooltip('要播放的动画名称'));
    this.data.push(new StringValue('动画时间', 'time', '1000')
        .setTooltip('要播放的动画时间'));
}
extend('MechanicGermAnimationStop', 'Component');
function MechanicGermAnimationStop() {
    this.super('Germ Animation Stop', Type.MECHANIC, true);

    this.description = '停止播放萌芽动作';

    this.data.push(new StringValue('动画名称', 'name', 'default')
        .setTooltip('要停止播放的动画名称'));
    this.data.push(new StringValue('动画时间', 'time', '1000')
        .setTooltip('要停止播放的动画时间'));
}

extend('MechanicReturn', 'Component');
function MechanicReturn() {
    this.super('Return', Type.MECHANIC, true);

    this.description = '中断 Delay';
    this.data.push(new StringValue('标记', 'mark', '')
        .setTooltip('要中断的标记名称，该delay必须设置了标记, 可使用 ; 分割多个标记')
    );

    this.data.push(new StringValue('禁止发包行为', 'deny-germ-action', '')
        .setTooltip('当 delay 被打断时为目标执行的动作')
    );

    this.data.push(new StringValue('禁止指令', 'deny-commands', '')
        .setTooltip('当 delay 被打断时为目标执行的指令')
    );
}


extend('MechanicMythicCast', 'Component');

function MechanicMythicCast() {
    this.super('Mythic Cast', Type.MECHANIC, false);

    this.description = '让目标释放Mythic的技能组';

    this.data.push(new StringValue('技能名', 'skillname', '技能名')
        .setTooltip('唯一识别标签 {uuid} 会被进行替换为 施法者的UUID')
    );
    this.data.push(new AttributeValue('强度', 'power', 0, 0)
        .setTooltip('技能等级')
    );
}

extend('MechanicMythicCastTarget', 'Component');
function MechanicMythicCastTarget() {
    this.super('Mythic Cast Target', Type.MECHANIC, false);

    this.description = '对目标释放技能';

    this.data.push(new StringValue('技能名', 'skillname', '技能名')
        .setTooltip('唯一识别标签 {uuid} 会被进行替换为 施法者的UUID')
    );
    this.data.push(new AttributeValue('强度', 'power', 0, 0)
        .setTooltip('技能等级')
    );
}

extend('MechanicArenaShow', 'Component');
function MechanicArenaShow() {
    this.super('Arena Showt', Type.MECHANIC, false);

    this.description = '让指定坐标位置附近的草丛玩家现形';

    this.data.push(new AttributeValue('计时器', 'timer', 2, 0)
        .setTooltip('现形时间')
    );

    this.data.push(new AttributeValue('范围', 'range', 10, 0)
        .setTooltip('现形范围')
    );
}

// MechanicSkillCast
extend('MechanicSkillCast', 'Component');
function MechanicSkillCast() {
    this.super('Skill cast', Type.MECHANIC, false);

    this.description = '执行另外一个SK技能';

    this.data.push(new StringValue('技能', 'skill', "")
        .setTooltip('技能名称')
    );

}

// MechanicMythicSummonDelete
extend('MechanicMythicSummonDelete', 'Component');
function MechanicMythicSummonDelete() {
    this.super('Mythic Summon Delete', Type.MECHANIC, false);

    this.description = '移除目标指定类型的召唤物';

    this.data.push(new StringValue('神话类型', 'mythicType', "default")
        .setTooltip('MM 生物配置的 ID 非怪物展示名称')
    );

}

extend('MechanicMythicSummonValue', 'Component');
function MechanicMythicSummonValue() {
    this.super('Mythic Summon Value', Type.MECHANIC, false);
    this.description = '将目标指定召唤物总存在数更新至 Value 容器，共 Value 操作器使用';
    this.data.push(new StringValue('神话类型', 'mythicType', "default")
        .setTooltip('MM 生物配置的 ID 非怪物展示名称')
    );
    this.data.push(new StringValue('键名', 'key', "default")
        .setTooltip('存活数存入 Value 容器时使用的 KEY')
    );

}

extend('MechanicBadgeCooldownReset', 'Component');
function MechanicBadgeCooldownReset() {
    this.super('Badge Cooldown Reset', Type.MECHANIC, false);
    this.description = '重置徽章冷却时间';
    this.data.push(new ListValue('类型', 'type', ["R", "E", "ALL"], "ALL")
        .setTooltip('重置什么按键的')
    );
}
// MechanicBadgeCooldownScale
extend('MechanicBadgeCooldownScale', 'Component');
function MechanicBadgeCooldownScale() {
    this.super('Badge Cooldown Scale', Type.MECHANIC, false);
    this.description = '实在当前徽章技能冷却缩减百分比';
    this.data.push(new ListValue('类型', 'type', ["R", "E", "ALL"], "ALL")
        .setTooltip('重置什么按键的')
    );
    this.data.push(new AttributeValue('比例', 'proportion', 0.1, 0)
        .setTooltip('缩减比例')
    );
}

// MechanicTrailStart
extend('MechanicTrailStart', 'Component');
function MechanicTrailStart() {
    this.super('Trail Start', Type.MECHANIC, false);
    this.description = '路径追踪';
    this.data.push(new StringValue('特效名称', 'effectName', "default")
        .setTooltip('萌芽特效名称')
    );
    this.data.push(new AttributeValue('半径', 'radius', 0.5, 0)
        .setTooltip('单点范围')
    );
    this.data.push(new AttributeValue('持续时间', 'duration', -1, 0)
        .setTooltip('整体存活时间')
    );
    this.data.push(new AttributeValue('分段持续时间', 'segmentDuration', 3, 0)
        .setTooltip('路径存活时间')
    );
    this.data.push(new AttributeValue('特效时机', 'effectTiming', 3, 0)
        .setTooltip('特效存活时间')
    );
    this.data.push(new AttributeValue('命中持续时间', 'hitDuration', 1, 0)
        .setTooltip('命中间隔')
    );
}

extend('MechanicTrailEnd', 'Component');
function MechanicTrailEnd() {
    this.super('Trail End', Type.MECHANIC, false);
    this.description = '路径追踪结束';
    this.data.push(new StringValue('特效名称', 'effectName', "default")
        .setTooltip('萌芽特效名称')
    );
}

extend('MechanicRunicReduce', 'Component');
function MechanicRunicReduce()
{
    this.super('Runic Reduce', Type.MECHANIC, false);

    this.description = '设置特定runic的冷却缩减比例.';

    this.data.push(new StringListValue('键名', 'key', [ 'Default' ])
        .setTooltip('要进行缩减的 runic 名称')
    );
    this.data.push(new AttributeValue('缩放', 'scale', 0.5, 0.0)
        .setTooltip('缩减比例 <0.0-1.0> 其中 0.5=50%')
    );
}
// MechanicRevive
extend('MechanicRevive', 'Component');
function MechanicRevive()
{
    this.super('Revive', Type.MECHANIC, false);

    this.description = '复活目标玩家，前提是目标处于倒地状态.';

}
// MechanicNeonflashAttribute
extend('MechanicNeonflashAttribute', 'Component');
function MechanicNeonflashAttribute()
{
    this.super('Neonflash Attribute', Type.MECHANIC, false);

    this.description = '设置 NF 属性容器，支持调用SK的value容器值作为填充';

    this.data.push(new StringValue('属性名称', 'key', "暴击概率")
        .setTooltip('被修改的属性名称')
    );

    this.data.push(new ListValue('是否调用SK容器值', 'useValue', ['True', 'False'], 'True')
        .setTooltip('调用SK的value容器值作为填充')
    );

    this.data.push(new StringValue('SK容器value', 'skKey', 'key name')
        .requireValue('useValue', [ 'True' ])
        .setTooltip('填写位于SK容器中的value key 用于获取值')
    );

    this.data.push(new DoubleValue('数量', 'amount', 0.0)
        .requireValue('useValue', [ 'False' ])
        .setTooltip('填写位于SK容器中的value key 用于获取值')
    );

    this.data.push(new AttributeValue('时长', 'seconds', -1.0, 0)
        .requireValue('subtract', [ 'False' ])
        .setTooltip('缩减比例')
    );

    this.data.push(new ListValue('是否减法容器', 'subtract', ['True', 'False'], 'False')
        .setTooltip('这将导致对全局属性进行减法输出')
    );

    this.data.push(new StringListValue('属性容器名称', 'container', [ 'geekteamplus:runic', 'neonarena:runic'])
        .requireValue('subtract', [ 'False' ])
        .setTooltip('目标容器名称')
    );

    this.data.push(new ListValue('是否作为倍率属性', 'scale', ['True', 'False'], 'False')
        .requireValue('subtract', [ 'False' ])
        .setTooltip('该值会覆盖全局属性设置选项，修改该属性在本次容器是否承担倍率变更')
    );

    this.data.push(new ListValue('是否覆盖', 'stackable', ['True', 'False'], 'True')
        .requireValue('subtract', [ 'False' ])
        .setTooltip('没次变更覆盖上一次设置')
    );

}

// MechanicGravityField
extend('MechanicGravityField', 'Component');
function MechanicGravityField()
{
    this.super('Gravity Field', Type.MECHANIC, false);

    this.description = '引力场';

    this.data.push(new AttributeValue('范围', 'radius', 5, 0)
        .setTooltip('范围')
    );

    this.data.push(new AttributeValue('拉力', 'pullStrength', 0.15, 0)
        .setTooltip('拉力')
    );

    this.data.push(new AttributeValue('时长', 'duration', 5, 0)
        .setTooltip('整体存活时间')
    );

    this.data.push(new ListValue('模式', 'decayMode', [ 'CONSTANT', 'LINEAR_INVERSE', 'LINEAR_PROPORTIONAL'], 'CONSTANT')
        .setTooltip('模式')
    );

    this.data.push(new ListValue('目标种类', 'targetType', [ 'all', 'ot', 'ally'], 'all')
        .setTooltip('目标筛选')
    );


}

extend('MechanicBuffHud', 'Component');
function MechanicBuffHud()
{
    this.super('Buff Hud', Type.MECHANIC, false);

    this.description = 'buffHud操作';

    this.data.push(new ListValue('动作类型', 'action', [ 'insert', 'delete', 'marge'], 'insert')
        .setTooltip('insert(插入)、delete(删除)、marge(合并)')
    );

    this.data.push(new StringValue('键', 'key', 'DEFAULT')
        .setTooltip('该hud组件的唯一识别键')
    );

    this.data.push(new StringValue('纹理图标', 'texture', 'local<->textures/hud/zd.png')
        .setTooltip('hud显示的图标')
    );

    this.data.push(new StringListValue('文本', 'texts', [ '1'])
        .setTooltip('hud图标处显示的文本，如果显示叠层的那种数字，直接写 1、2、3，可以多行')
    );

    this.data.push(new AttributeValue('时长', 'time', -1.0, 0)
        .setTooltip('-1.0视为永不过期，可以通过合并和重新插入一样的key重置时间')
    );

}

extend('MechanicGermDos', 'Component');
function MechanicGermDos()
{
    this.super('Germ dos', Type.MECHANIC, false);

    this.description = '执行萌芽dos';

    this.data.push(new StringValue('萌芽 DOS', 'dos', 'DEFULT')
        .setTooltip('要执行的萌芽dos')
    );

}

extend('MechanicCrak', 'Component');
function MechanicCrak()
{
    this.super('Crack', Type.MECHANIC, false);

    this.description = '地面（地裂）特效';

    this.data.push(new ListValue('效果类型', 'type', [ 'WAVE', 'EXPLODE', 'SQUEEZE', 'SPIN_FLY'], 'WAVE')
        .setTooltip('效果类型')
    );

    this.data.push(new AttributeValue('索引', 'index', 1, 0)
        .setTooltip('特效id，尽量别重复')
    );

    this.data.push(new AttributeValue('范围', 'radius', 3.0, 0)
        .setTooltip('单点特效范围')
    );

    this.data.push(new AttributeValue('力度', 'strength', 1.0, 0)
        .setTooltip('方块受力力度')
    );

    this.data.push(new AttributeValue('时长', 'duration', 40.0, 0)
        .setTooltip('特效存留时长')
    );

    this.data.push(new AttributeValue('点长度', 'length', 1.0, 0)
        .setTooltip('生成几个点')
    );

    this.data.push(new AttributeValue('可见范围', 'range', 64.0, 0)
        .setTooltip('控制当前世界内可见的玩家范围')
    );

}

// MechanicInterruptParry
extend('MechanicInterruptParry', 'Component');
function MechanicInterruptParry()
{
    this.super('Interrupt Parry', Type.MECHANIC, false);

    this.description = '中断等待中的格挡姿态';

}

// The active component being edited or added to
var activeComponent = undefined;

/**
 * Adds the options for item related effects to the component
 *
 * @param {Component} component - the component to add to
 */
function addItemOptions(component) {
    
    component.data.push(new ListValue('检查材质', 'check-mat', [ 'True', 'False' ], 'True')
        .setTooltip('是否要求物品为指定类型')
    );
    component.data.push(new ListValue('材质', 'material', materialList, 'Arrow')
        .requireValue('check-mat', [ 'True' ])
        .setTooltip('物品需要的类型')
    );
    
    component.data.push(new ListValue('检查数据', 'check-data', [ 'True', 'False' ], 'False')
        .setTooltip('是否要求物品具有指定数据值')
    );
    component.data.push(new IntValue('数据', 'data', 0)
        .requireValue('check-data', [ 'True' ])
        .setTooltip('物品必须具有的数据值')
    );
    
    component.data.push(new ListValue('检查 Lore', 'check-lore', [ 'True', 'False' ], 'False')
        .setTooltip('是否要求物品 Lore 中包含某段文字')
    );
    component.data.push(new StringValue('Lore', 'lore', 'text')
        .requireValue('check-lore', [ 'True' ])
        .setTooltip('物品 Lore 中需要的文字')
    );
    
    component.data.push(new ListValue('检查名称', 'check-name', [ 'True', 'False' ], 'False')
        .setTooltip('是否要求物品显示名称中包含某段文字')
    );
    component.data.push(new StringValue('名称', 'name', 'name')
        .requireValue('check-name', [ 'True' ])
        .setTooltip('物品显示名称中需要的文字')
    );
    
    component.data.push(new ListValue('正则', 'regex', [ 'True', 'False' ], 'False')
        .setTooltip('名称与 Lore 检查是否按正则表达式处理。不了解正则可保持默认。')
    );
}

function addProjectileOptions(component) {
    
    // General data
    component.data.push(new ListValue("分组", "group", ["Ally", "Enemy"], "Enemy")
        .setTooltip('命中实体的筛选方式')
    );
    component.data.push(new ListValue('扩散', 'spread', [ 'Cone', 'Horizontal Cone', 'Rain' ], 'Cone')
        .setTooltip('发射抛射物的方式。Cone 以准星为中心呈锥形发射；Horizontal cone 与 Cone 相同但锁定在 XZ 水平面；Rain 从目标上方落下。若只想笔直发射一支箭，请用 Cone')
    );
    component.data.push(new AttributeValue('数量', 'amount', 1, 0)
        .setTooltip('发射的抛射物数量')
    );
    component.data.push(new AttributeValue('速度', 'velocity', 3, 0)
        .setTooltip('抛射物发射速度，负值表示反方向发射。')
    );
    
    // Cone values
    component.data.push(new AttributeValue('角度', 'angle', 30, 0)
        .requireValue('spread', [ 'Cone', 'Horizontal Cone' ])
        .setTooltip('抛射物散布的锥形角度（度）。只发射一个抛射物时该项无效。')
    );
    component.data.push(new DoubleValue('位置', 'position', 0, 0)
        .requireValue('spread', [ 'Cone', 'Horizontal Cone' ])
        .setTooltip('抛射物的起始高度（相对地面）')
    );
    
    // Rain values
    component.data.push(new AttributeValue('高度', 'height', 8, 0)
        .requireValue('spread', [ 'Rain' ])
        .setTooltip('从目标上方多少格开始落下抛射物')
    );
    component.data.push(new AttributeValue('半径', 'rain-radius', 2, 0)
        .requireValue('spread', [ 'Rain' ])
        .setTooltip('落雨区域的半径（格）')
    );
    
    // Offsets
    component.data.push(new AttributeValue('前向偏移', 'forward', 0, 0)
        .setTooltip('抛射物在目标前方多少格发射，负值表示身后。')
    );
    component.data.push(new AttributeValue('上向偏移', 'upward', 0, 0)
        .setTooltip('抛射物在目标上方多少格发射，负值表示下方。')
    );
    component.data.push(new AttributeValue('右偏移', 'right', 0, 0)
        .setTooltip('抛射物在目标右侧多少格发射，负值表示左侧。')
    );
}

/**
 * Adds the options for particle effects to the components
 *
 * @param {Component} component - the component to add to
 */
function addParticleOptions(component) {
    component.data.push(new ListValue('粒子', 'particle', 
        [ 
            'Angry Villager', 
            'Barrier',
            'Block Crack', 
            'Bubble', 
            'Cloud', 
            'Crit', 
            'Damage Indicator',
            'Death', 
            'Death Suspend', 
            'Dragon Breath',
            'Drip Lava', 
            'Drip Water', 
            'Enchantment Table', 
            'End Rod',
            'Ender Signal', 
            'Explode', 
            'Firework Spark', 
            'Flame', 
            'Footstep', 
            'Happy Villager', 
            'Heart', 
            'Huge Explosion', 
            'Hurt', 
            'Icon Crack', 
            'Instant Spell', 
            'Large Explode', 
            'Large Smoke', 
            'Lava', 
            'Magic Crit', 
            'Mob Spell', 
            'Mob Spell Ambient', 
            'Mobspawner Flames', 
            'Note', 
            'Portal', 
            'Potion Break', 
            'Red Dust', 
            'Sheep Eat', 
            'Slime', 
            'Smoke', 
            'Snowball Poof', 
            'Snow Shovel', 
            'Spell', 
            'Splash', 
            'Sweep Attack',
            'Suspend', 
            'Town Aura', 
            'Water Drop',
            'Water Wake',
            'Witch Magic', 
            'Wolf Hearts', 
            'Wolf Shake', 
            'Wolf Smoke' 
        ], 'Angry Villager')
        .setTooltip('要显示的粒子类型。带 DX、DY、DZ 选项的粒子特效与 Cauldron 不兼容')
    );
    
    component.data.push(new ListValue('材质', 'material', materialList, 'Dirt').requireValue('particle', [ 'Block Crack', 'Icon Crack' ])
        .setTooltip('Block Crack 或 Icon Crack 粒子使用的材质')
    );
    component.data.push(new IntValue('类型', 'type', 0).requireValue('particle', [ 'Block Crack', 'Icon Crack' ])
        .setTooltip('Block Crack 或 Icon Crack 粒子使用的材质数据值')
    );
    
    component.data.push(new ListValue('排列方式', 'arrangement', [ 'Circle', 'Hemisphere', 'Sphere' ], 'Circle')
        .setTooltip('粒子的排列方式。Circle 为二维圆，Hemisphere 为半球，Sphere 为三维球')
    );
    component.data.push(new AttributeValue('半径', 'radius', 4, 0)
        .setTooltip('排列的半径（格）')
    );
    component.data.push(new AttributeValue('粒子', 'particles', 20, 0)
        .setTooltip('粒子数量')
    );
    
    // Circle arrangement direction
    component.data.push(new ListValue('环形方向', 'direction', [ 'XY', 'XZ', 'YZ' ], 'XZ').requireValue('arrangement', [ 'Circle' ])
        .setTooltip('圆形排列的朝向。XY 与 YZ 为竖直圆，XZ 为水平圆。')
    );
    
    // Bukkit particle data value
    component.data.push(new IntValue('数据', 'data', 0).requireValue('particle', [ 'Smoke', 'Ender Signal', 'Mobspawner Flames', 'Potion Break' ])
        .setTooltip('粒子的数据值，不同粒子作用不同，例如烟雾粒子的朝向或药水破裂的颜色')
    );
    
    // Reflection particle data
    var reflectList = [ 'Angry Villager', 'Bubble', 'Cloud', 'Crit', 'Damage Indicator', 'Death Suspend', 'Dragon Breath', 'Drip Lava', 'Drip Water', 'Enchantment Table', 'End Rod', 'Explode', 'Fireworks Spark', 'Flame', 'Footstep', 'Happy Villager', 'Hear', 'Huge Explosion', 'Instant Spell', 'Large Explode', 'Large Smoke', 'Lava', 'Magic Crit', 'Mob Spell', 'Mob Spell Ambient', 'Note', 'Portal', 'Red Dust', 'Slime', 'Snowball Poof', 'Snow Shovel', 'Spell', 'Splash', 'Suspend', 'Sweep Attack', 'Town Aura', 'Water Drop', 'Water Wake', 'Witch Magic' ];
    component.data.push(new IntValue('可见半径', 'visible-radius', 25).requireValue('particle', reflectList)
        .setTooltip('玩家可见粒子的最远距离（格）')
    );
    component.data.push(new DoubleValue('X 偏移', 'dx', 0).requireValue('particle', reflectList)
        .setTooltip('发包参数，因粒子而异，通常表示粒子在 X 方向上离中心的最大偏移。')
    );
    component.data.push(new DoubleValue('Y 偏移', 'dy', 0).requireValue('particle', reflectList)
        .setTooltip('发包参数，因粒子而异，通常表示粒子在 Y 方向上离中心的最大偏移。')
    );
    component.data.push(new DoubleValue('Z 偏移', 'dz', 0).requireValue('particle', reflectList)
        .setTooltip('发包参数，因粒子而异，通常表示粒子在 Z 方向上离中心的最大偏移。')
    );
    component.data.push(new DoubleValue('粒子速度', 'speed', 1).requireValue('particle', reflectList)
        .setTooltip('发包参数，因粒子而异，通常控制粒子的颜色或速度。')
    );
    component.data.push(new DoubleValue('发包数量', 'amount', 1).requireValue('particle', reflectList)
        .setTooltip('发包参数，因粒子而异。设为 0 可让部分粒子支持自定义颜色。')
    );
}

function addEffectOptions(component, optional)
{
    var opt = appendNone;
    if (optional)
    {
        opt = appendOptional;
        
        component.data.push(new ListValue('使用特效', 'use-effect', [ 'True', 'False' ], 'False')
            .setTooltip('是否使用高级粒子特效。')
        );
    }
    
    component.data.push(opt(new StringValue('特效键', 'effect-key', 'default')
        .setTooltip('引用该特效的键，同一键同时只能有一个特效生效。')
    ));
    component.data.push(opt(new AttributeValue('持续时间', 'duration', 1, 0)
        .setTooltip('特效播放时长（秒）')
    ));
    
    component.data.push(opt(new StringValue('形状', '-shape', 'hexagon')
        .setTooltip('决定每次迭代粒子位置的公式键，可在 effects.yml 中查看已定义公式及其键。')
    ));
    component.data.push(opt(new ListValue('形状方向', '-shape-dir', [ 'XY', 'YZ', 'XZ' ], 'XY')
        .setTooltip('形状公式作用的平面。XZ 为水平面，其余两个为竖直面。')
    ));
    component.data.push(opt(new StringValue('形状尺寸', '-shape-size', '1')
        .setTooltip('决定形状大小的公式，可使用 wiki 中定义的任意运算符。')
    ));
    component.data.push(opt(new StringValue('动画', '-animation', 'one-circle')
        .setTooltip('决定粒子特效相对目标移动位置的公式键，可在 effects.yml 中查看已定义公式及其键。')
    ));
    component.data.push(opt(new ListValue('动画方向', '-anim-dir', [ 'XY', 'YZ', 'XZ' ], 'XZ')
        .setTooltip('动画运动所在的平面。XZ 为水平面，其余两个为竖直面。')
    ));
    component.data.push(opt(new StringValue('动画尺寸', '-anim-size', '1')
        .setTooltip('决定动画距离倍率的公式，可使用 wiki 中定义的任意运算符。')
    ));
    component.data.push(opt(new IntValue('间隔', '-interval', 1)
        .setTooltip('播放粒子的间隔 tick 数。')
    ));
    component.data.push(opt(new IntValue('视距', '-view-range', 25)
        .setTooltip('特效可见的最远距离')
    ));
    
    component.data.push(opt(new ListValue('粒子类型', '-particle-type', [
            'BARRIER',
            'BLOCK_CRACK',
            'CLOUD',
            'CRIT',
            'CRIT_MAGIC',
            'DAMAGE_INDICATOR',
            'DRAGON_BREATH',
            'DRIP_LAVA',
            'DRIP_WATER',
            'ENCHANTMENT_TABLE',
            'END_ROD',
            'EXPLOSION_HUGE',
            'EXPLOSION_LARGE',
            'EXPLOSION_NORMAL',
            'FIREWORKS_SPARK',
            'FLAME',
            'FOOTSTEP',
            'HEART',
            'LAVA',
            'MOB_APPEARANCE',
            'NOTE',
            'PORTAL',
            'REDSTONE',
            'SLIME',
            'SMOKE_NORMAL',
            'SMOKE_LARGE',
            'SNOWBALL',
            'SNOW_SHOVEL',
            'SPELL',
            'SPELL_INSTANT',
            'SPELL_MOB',
            'SPELL_MOB_AMBIENT',
            'SPELL_WITCH',
            'SUSPEND_DEPTH',
            'SUSPENDED',
            'SWEEP_ATTACK',
            'TOWN_AURA',
            'VILLAGER_ANGRY',
            'VILLAGER_HAPPY',
            'WATER_BUBBLE',
            'WATER_SPLASH',
            'WATER_WAKE'
        ], 'BARRIER')
        .setTooltip('要使用的粒子类型')
    ));
    component.data.push(opt(new ListValue('粒子材质', '-particle-material', materialList, 'WOOD')
        .requireValue('-particle-type', [ 'BLOCK_CRACK' ])
        .setTooltip('粒子使用的材质')
    ));
    component.data.push(opt(new IntValue('粒子数据', '-particle-data', 0)
        .requireValue('-particle-type', [ 'BLOCK_CRACK' ])
        .setTooltip('粒子材质的数值')
    ));
    component.data.push(opt(new IntValue('粒子数量', '-particle-amount', 0)
        .setTooltip('粒子的整数数据，通常标记为 amount')
    ));
    component.data.push(opt(new DoubleValue('X 偏移', '-particle-dx', 0)
        .setTooltip('粒子的 DX 值，常用于颜色')
    ));
    component.data.push(opt(new DoubleValue('Y 偏移', '-particle-dy', 0)
        .setTooltip('粒子的 DY 值，常用于颜色')
    ));
    component.data.push(opt(new DoubleValue('Z 偏移', '-particle-dz', 0)
        .setTooltip('粒子的 DZ 值，常用于颜色')
    ));
    component.data.push(opt(new DoubleValue('速度', '-particle-speed', 1)
        .setTooltip('粒子速度值，有时与运动速度相关')
    ));
}

function appendOptional(value)
{
    value.requireValue('use-effect', [ 'True' ]);
    return value;
}

function appendNone(value)
{
    return value;
}
