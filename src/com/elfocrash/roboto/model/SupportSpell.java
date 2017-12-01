package com.elfocrash.roboto.model;

public class SupportSpell {
	private int _skillId;
	private SupportSpellUsageCondition _condition;
	private int _conditionValue;
	
	public SupportSpell (int skillId, SupportSpellUsageCondition condition, int conditionValue) {
		_skillId = skillId;	
		_condition = condition;
		_conditionValue = conditionValue;
	}
	
	public SupportSpell (int skillId, SupportSpellUsageCondition condition) {
		_skillId = skillId;	
		_condition = condition;
		_conditionValue = 0;
	}
	
	public int getSkillId() {
		return _skillId;
	}
	
	public SupportSpellUsageCondition getCondition(){
		return _condition;
	}
	
	public int getConditionValue() {
		return _conditionValue;
	}
}