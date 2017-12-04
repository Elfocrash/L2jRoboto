package com.elfocrash.roboto.model;

public abstract class BotSkill {
	protected int _skillId;
	protected SpellUsageCondition _condition;
	protected int _conditionValue;
	
	public BotSkill(int skillId, SpellUsageCondition condition, int conditionValue) {
		_skillId = skillId;
		_condition = condition;
		_conditionValue = conditionValue;
	}

	public BotSkill(int skillId, SpellUsageCondition condition) {
		_skillId = skillId;
		_condition = condition;
		_conditionValue = 0;
	}
	
	public int getSkillId() {
		return _skillId;
	}	

	public SpellUsageCondition getCondition(){
		return _condition;
	}
	
	public int getConditionValue() {
		return _conditionValue;
	}
}
