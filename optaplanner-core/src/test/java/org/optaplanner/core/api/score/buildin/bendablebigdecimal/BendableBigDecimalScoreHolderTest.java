/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.api.score.buildin.bendablebigdecimal;

import java.math.BigDecimal;

import org.junit.Test;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.holder.AbstractScoreHolderTest;

import static org.junit.Assert.*;

public class BendableBigDecimalScoreHolderTest extends AbstractScoreHolderTest {

    @Test
    public void addConstraintMatchWithConstraintMatch() {
        addConstraintMatch(true);
    }

    @Test
    public void addConstraintMatchWithoutConstraintMatch() {
        addConstraintMatch(false);
    }

    public void addConstraintMatch(boolean constraintMatchEnabled) {
        BendableBigDecimalScoreHolder scoreHolder = new BendableBigDecimalScoreHolder(constraintMatchEnabled, 1, 2);

        RuleContext hard1 = mockRuleContext("hard1");
        scoreHolder.addHardConstraintMatch(hard1, 0, new BigDecimal("-0.01"));
        assertEquals(BendableBigDecimalScore.valueOf(new BigDecimal[]{new BigDecimal("-0.01")}, new BigDecimal[]{new BigDecimal("0.00"), new BigDecimal("0.00")}), scoreHolder.extractScore(0));

        RuleContext hard2Undo = mockRuleContext("hard2Undo");
        scoreHolder.addHardConstraintMatch(hard2Undo, 0, new BigDecimal("-0.08"));
        assertEquals(BendableBigDecimalScore.valueOf(new BigDecimal[]{new BigDecimal("-0.09")}, new BigDecimal[]{new BigDecimal("0.00"), new BigDecimal("0.00")}), scoreHolder.extractScore(0));
        callOnDelete(hard2Undo);
        assertEquals(BendableBigDecimalScore.valueOf(new BigDecimal[]{new BigDecimal("-0.01")}, new BigDecimal[]{new BigDecimal("0.00"), new BigDecimal("0.00")}), scoreHolder.extractScore(0));

        RuleContext medium1 = mockRuleContext("medium1");
        scoreHolder.addSoftConstraintMatch(medium1, 0, new BigDecimal("-0.10"));
        callOnUpdate(medium1);
        scoreHolder.addSoftConstraintMatch(medium1, 0, new BigDecimal("-0.20")); // Overwrite existing

        RuleContext soft1 = mockRuleContext("soft1", DEFAULT_JUSTIFICATION, OTHER_JUSTIFICATION);
        scoreHolder.addSoftConstraintMatch(soft1, 1, new BigDecimal("-1.00"));
        callOnUpdate(soft1);
        scoreHolder.addSoftConstraintMatch(soft1, 1, new BigDecimal("-3.00")); // Overwrite existing

        RuleContext multi1 = mockRuleContext("multi1");
        scoreHolder.addMultiConstraintMatch(multi1, new BigDecimal[]{new BigDecimal("-10.00")}, new BigDecimal[]{new BigDecimal("-100.00"), new BigDecimal("-1000.00")});
        callOnUpdate(multi1);
        scoreHolder.addMultiConstraintMatch(multi1, new BigDecimal[]{new BigDecimal("-40.00")}, new BigDecimal[]{new BigDecimal("-500.00"), new BigDecimal("-6000.00")}); // Overwrite existing

        RuleContext hard3 = mockRuleContext("hard3");
        scoreHolder.addHardConstraintMatch(hard3, 0, new BigDecimal("-10000.00"));
        callOnUpdate(hard3);
        scoreHolder.addHardConstraintMatch(hard3, 0, new BigDecimal("-70000.00")); // Overwrite existing

        RuleContext soft2Undo = mockRuleContext("soft2Undo", UNDO_JUSTIFICATION);
        scoreHolder.addSoftConstraintMatch(soft2Undo, 1, new BigDecimal("-0.99"));
        callOnDelete(soft2Undo);

        RuleContext multi2Undo = mockRuleContext("multi2Undo");
        scoreHolder.addMultiConstraintMatch(multi2Undo, new BigDecimal[]{new BigDecimal("-9.99")}, new BigDecimal[]{new BigDecimal("-9.99"), new BigDecimal("-9.99")});
        callOnDelete(multi2Undo);

        RuleContext medium2Undo = mockRuleContext("medium2Undo");
        scoreHolder.addSoftConstraintMatch(medium2Undo, 0, new BigDecimal("-99.99"));
        callOnDelete(medium2Undo);

        assertEquals(BendableBigDecimalScore.valueOf(new BigDecimal[]{new BigDecimal("-70040.01")}, new BigDecimal[]{new BigDecimal("-500.20"), new BigDecimal("-6003.00")}), scoreHolder.extractScore(0));
        assertEquals(BendableBigDecimalScore.valueOfUninitialized(-7, new BigDecimal[]{new BigDecimal("-70040.01")}, new BigDecimal[]{new BigDecimal("-500.20"), new BigDecimal("-6003.00")}), scoreHolder.extractScore(-7));
        if (constraintMatchEnabled) {
            assertEquals(BendableBigDecimalScore.valueOf(new BigDecimal[]{new BigDecimal("-0.01")}, new BigDecimal[]{new BigDecimal("0.00"), new BigDecimal("0.00")}), findConstraintMatchTotal(scoreHolder, "hard1").getScore());
            assertEquals(BendableBigDecimalScore.valueOf(new BigDecimal[]{new BigDecimal("0.00")}, new BigDecimal[]{new BigDecimal("0.00"), new BigDecimal("-3.00")}), scoreHolder.getIndictmentMap().get(OTHER_JUSTIFICATION).getScore());
            assertNull(scoreHolder.getIndictmentMap().get(UNDO_JUSTIFICATION));
        }
    }

}
