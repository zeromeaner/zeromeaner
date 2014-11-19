/*
    Copyright (c) 2010, NullNoname
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of NullNoname nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/
package org.zeromeaner.game.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zeromeaner.util.CustomProperties;


/**
 * Used in the replay button input dataClass of
 */
public class ReplayData implements Serializable {
	/** Serial version ID */
	private static final long serialVersionUID = 737226985994393117L;

	/** Button input dataOf default The length of the */
	public static final int DEFAULT_ARRAYLIST_SIZE = 60 * 60 * 10;

	/** Button input data */
	public ArrayList<Integer> inputDataArray;
	
	public ArrayList<Map<String, Integer>> additionalData;

	/**
	 * Default constructor
	 */
	public ReplayData() {
		reset();
	}

	/**
	 * Copy constructor
	 * @param r Copy source
	 */
	public ReplayData(ReplayData r) {
		copy(r);
	}

	/**
	 * Reset to defaults
	 */
	public void reset() {
		if(inputDataArray == null)
			inputDataArray = new ArrayList<Integer>(DEFAULT_ARRAYLIST_SIZE);
		else
			inputDataArray.clear();
		additionalData = new ArrayList<>();
	}

	/**
	 * OtherReplayDataCopied from the
	 * @param r Copy source
	 */
	public void copy(ReplayData r) {
		reset();

		for(int i = 0; i < r.inputDataArray.size(); i++) {
			inputDataArray.add(i, r.inputDataArray.get(i));
		}
	}

	/**
	 *  button inputSet the status
	 * @param input  button inputBit of status flag
	 * @param frame  frame  (Course time)
	 */
	public void setInputData(int input, int frame) {
		if((frame < 0) || (frame >= inputDataArray.size())) {
			inputDataArray.add(input);
		} else {
			inputDataArray.set(frame, input);
		}
	}

	public void setAdditionalData(String key, int value, int frame) {
		while(additionalData.size() <= frame)
			additionalData.add(new TreeMap<String, Integer>());
		additionalData.get(frame).put(key, value);
	}
	
	/**
	 *  button inputGet status
	 * @param frame  frame  (Course time)
	 * @return  button inputBit of status flag
	 */
	public int getInputData(int frame) {
		if((frame < 0) || (frame >= inputDataArray.size())) {
			return 0;
		}
		return inputDataArray.get(frame);
	}
	
	public Integer getAdditionalData(String key, int frame) {
		if(frame >= additionalData.size())
			return null;
		return additionalData.get(frame).get(key);
	}

	/**
	 * Stored in the property set
	 * @param p Property Set
	 * @param id AnyID (Player IDEtc.)
	 * @param maxFrame Save frame count (-1Save in all)
	 */
	public void writeProperty(CustomProperties p, int id, int maxFrame) {
		int max = maxFrame;
		if((maxFrame < 0) || (maxFrame > inputDataArray.size())) max = inputDataArray.size();

		for(int i = 0; i < max; i++) {
			int input = getInputData(i);
			int previous = getInputData(i - 1);
			if(input != previous) p.setProperty(id + ".r." + i, input);
			
			if(i < additionalData.size()) {
				for(Entry<String, Integer> e : additionalData.get(i).entrySet()) {
					p.setProperty(id + ".r." + e.getKey() + "." + i, e.getValue());
				}
			}
		}
		p.setProperty(id + ".r.max", max);
	}

	public int max() {
		return inputDataArray.size();
	}
	
	/**
	 * Read from the property set
	 * @param p Property Set
	 * @param id AnyID (Player IDEtc.)
	 */
	public void readProperty(CustomProperties p, int id) {
		reset();
		int max = p.getProperty(id + ".r.max", 0);
		int input = 0;

		for(int i = 0; i < max; i++) {
			int data = p.getProperty(id + ".r." + i, -1);
			if(data != -1) input = data;
			setInputData(input, i);
		}
		
		Pattern pattern = Pattern.compile(id + "\\.r\\.(.*)\\.(\\d+)");
		for(String prop : p.stringPropertyNames()) {
			Matcher m = pattern.matcher(prop);
			if(!m.matches())
				continue;
			String key = m.group(1);
			int frame = Integer.parseInt(m.group(2));
			setAdditionalData(key, Integer.parseInt(p.getProperty(prop)), frame);
		}
	}
}
