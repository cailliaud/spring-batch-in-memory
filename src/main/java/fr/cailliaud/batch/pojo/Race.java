package fr.cailliaud.batch.pojo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum Race {
    @XmlEnumValue("Human")
    HUMAN,
    @XmlEnumValue("Troll")
    TROLL,
    @XmlEnumValue("Elf")
    ELF,
    @XmlEnumValue("Undead")
    UNDEAD
}
