package com.jwlz.sjjc.zstp.utils.pkumod.result;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.Serializable;
import java.util.*;
import java.util.stream.IntStream;

/**
 * @author wj
 */
public class Record implements Serializable {

    private String[] vars;

    private Value[] values;

    private List<ImmutablePair<String, Value>> fields;

    public Record(String[] vars, Value[] values) {
        this.vars = vars;
        this.values = values;
        this.fields = new ArrayList<>();
        for (int i = 0; i < this.vars.length; i++) {
            this.fields.add(new ImmutablePair<String, Value>(vars[i], values[i]));
        }
    }

    public int index(String var) {
        OptionalInt indexOpt = IntStream.range(0, vars.length).filter(i -> var.equals(vars[i])).findFirst();
        if (indexOpt.isPresent()) {
            return indexOpt.getAsInt();
        } else {
            throw new NoSuchElementException("Unknown key: " + var);
        }
    }

    public Value get(int idx) {
        return this.values[idx];
    }

    public Value get(String var) {
        int varIndex = index(var);
        return this.fields.get(varIndex).right;
    }

    public List<Value> values() {
        return Arrays.asList(this.values);
    }

    public List<String> vars() {
        return Arrays.asList(this.vars);
    }


    public String[] getVars() {
        return this.vars;
    }

    public Value[] getValues() {
        return this.values;
    }
}
