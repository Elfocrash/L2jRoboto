package com.elfocrash.roboto.ai.walker;

import com.elfocrash.roboto.FakePlayer;
import com.elfocrash.roboto.model.WalkNode;
import com.elfocrash.roboto.model.WalkerType;

import net.sf.l2j.commons.random.Rnd;

public class GiranWalkerAI extends WalkerAI {
	
	public GiranWalkerAI(FakePlayer character) {
		super(character);
		_walkNodes.add(new WalkNode(82248, 148600, -3464, Rnd.get(5, 20)));
		_walkNodes.add(new WalkNode(82072, 147560, -3464, Rnd.get(5, 20)));
		_walkNodes.add(new WalkNode(82792, 147832, -3464, Rnd.get(5, 20)));
		_walkNodes.add(new WalkNode(83320, 147976, -3400, Rnd.get(5, 20)));
		_walkNodes.add(new WalkNode(84584, 148536, -3400, Rnd.get(5, 20)));
		_walkNodes.add(new WalkNode(83384, 149256, -3400, Rnd.get(5, 20)));		
	}
	
	@Override
	public void setup() {
		super.setup();
		
	}

	@Override
	protected WalkerType getWalkerType() {
		return WalkerType.RANDOM;
	}
}
