package com.elfocrash.roboto.model;

public class OffensiveSpell extends BotSkill {	
	
	private int _priority;
	
	public OffensiveSpell (int skillId, SpellUsageCondition condition, int conditionValue, int priority) {
		super(skillId, condition, conditionValue);
		_priority = priority;
	}
	
	public OffensiveSpell (int skillId, SpellUsageCondition condition, int priority) {
		super(skillId, condition, 0);
		_priority = priority;
	}	
	
	
	public int getPriority() {
		return _priority;
	}
}