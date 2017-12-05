package com.elfocrash.roboto.model;

import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;

public class HealingSpell extends BotSkill {
	
	private SkillTargetType _targetType;
	
	public HealingSpell (int skillId, SkillTargetType targetType, SpellUsageCondition condition, int conditionValue, int priority) {
		super(skillId, condition, conditionValue, priority);		
		_targetType = targetType;	
	}
	
	public HealingSpell (int skillId, SkillTargetType targetType, int conditionValue, int priority) {
		super(skillId, SpellUsageCondition.LESSHPPERCENT, conditionValue, priority);
		_targetType = targetType;	
	}
	
	public SkillTargetType getTargetType() {
		return _targetType;
	}
}