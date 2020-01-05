package fr.cailliaud.batch.pojo;

import lombok.*;

import javax.xml.bind.annotation.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@XmlRootElement(name = "hero")
public class Hero {

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "race")
    private Race race;
}
