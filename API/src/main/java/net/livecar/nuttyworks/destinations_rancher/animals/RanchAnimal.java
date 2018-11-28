package net.livecar.nuttyworks.destinations_rancher.animals;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;

public interface RanchAnimal
{
    RanchAnimal getRanchAnimal(Entity animal);
    Entity getEntity();

    int getAge();
    boolean canBreed();
    void setInLove();
    boolean isAdult();
    double getHealth();
    void damageAnimal(int damage);

    void setTamed(boolean isTame);

    //Sheep stuff
    boolean isSheered();
    boolean shearWool();
    DyeColor getColor();
    Material getWool();


}