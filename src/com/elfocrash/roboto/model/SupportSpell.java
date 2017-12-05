package com.elfocrash.roboto.model;

public class SupportSpell extends BotSkill {

	public SupportSpell(int skillId, SpellUsageCondition condition, int conditionValue, int priority) {
		super(skillId, condition, conditionValue, priority);
	}
	
	public SupportSpell(int skillId, int priority) {
		super(skillId, SpellUsageCondition.NONE, 0, priority);
	}
	
}