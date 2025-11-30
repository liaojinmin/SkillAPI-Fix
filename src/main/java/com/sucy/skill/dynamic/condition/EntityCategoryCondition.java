/**
 * SkillAPI
 * com.sucy.condition.dynamic.skill.EntityTypeCondition
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.dynamic.condition;

import com.rit.sucy.config.parse.DataSection;
import com.sucy.skill.dynamic.DynamicSkill;
import me.neon.core.toughness.ToughnessData;
import me.neon.libs.util.MetaKt;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class EntityCategoryCondition extends ConditionComponent {

    private enum Category {

        PLAYER, BOSS, MOB

    }

    private static final String TYPE = "category";

    private Set<Category> category;

    @Override
    public String getKey() {
        return "entity category";
    }

    @Override
    public void load(DynamicSkill skill, DataSection config) {
        super.load(skill, config);
        category = settings.getStringList(TYPE).stream()
                .map(s -> Category.valueOf(s.toUpperCase(Locale.ROOT)))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        boolean isPlayer = target instanceof Player;
        boolean isBoss = false;

        MetadataValue value = MetaKt.getMetaFirstOrNull(target, "Toughness");
        if (value != null) {
            Object o = value.value();
            if (o instanceof ToughnessData) {
                isBoss = ((ToughnessData) o).getBoss();
            }
        }

        // 3️⃣ 确定当前目标类别
        Category targetCategory;
        if (isPlayer) {
            targetCategory = Category.PLAYER;
        } else if (isBoss) {
            targetCategory = Category.BOSS;
        } else {
            targetCategory = Category.MOB;
        }

        // 4️⃣ 只要配置中包含该类别即可返回 true
        return category.contains(targetCategory);
    }
}
