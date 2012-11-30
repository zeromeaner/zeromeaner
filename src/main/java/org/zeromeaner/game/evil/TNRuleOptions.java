package org.zeromeaner.game.evil;

import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.util.GeneralUtil;

public class TNRuleOptions extends RuleOptions {
	public TNRuleOptions(RuleOptions opt) {
		copy(GeneralUtil.loadRule("config/rule/Standard.rul"));
		strRandomizer = opt.strRandomizer;
		strWallkick = "org.zeromeaner.game.subsystem.wallkick.StandardWallkick";
		strRuleName = "EVILINE";
		nextDisplay = 0;
		holdEnable = false;
		holdInitial = false;
		minARE = 0;
		maxARE = 0;
		minARELine = 0;
		maxARELine = 0;
		rotateWallkick = true;
	}
}
