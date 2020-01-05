package fr.cailliaud.batch.pojo;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum Race {
    @XmlEnumValue("Human")
    HUMAN("Human"),
    @XmlEnumValue("Troll")
    TROLL("Troll"),
    @XmlEnumValue("Elf")
    ELF("Elf"),
    @XmlEnumValue("Undead")
    UNDEAD("Undead");

    private String label;

    Race(String label){
        this.label=label;
    }

    public String getLabel(){
        return this.label;
    }

}
