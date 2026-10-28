package net.BlockNes.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class AttributeMap {
    private final Map<String, Attribute> attributes = new LinkedHashMap<>();

    public void addAttribute(Attribute attribute) { attributes.put(attribute.getName(), attribute); }

    public Attribute getAttribute(String name) { return attributes.get(name); }

    public boolean contains(String name) { return attributes.containsKey(name); }

    public Collection<Attribute> getAll() { return Collections.unmodifiableCollection(attributes.values()); }
}
