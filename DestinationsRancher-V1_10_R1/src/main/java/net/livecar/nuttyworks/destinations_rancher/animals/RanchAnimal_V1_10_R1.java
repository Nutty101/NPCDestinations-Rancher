package net.livecar.nuttyworks.destinations_rancher.animals;

import net.minecraft.server.v1_10_R1.EntityAnimal;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftSheep;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftTameableAnimal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class RanchAnimal_V1_10_R1 implements RanchAnimal {

    private Entity referencedAnimal = null;
    private CraftAnimals craftAnimal = null;

    public RanchAnimal_V1_10_R1() {
    }

    public RanchAnimal_V1_10_R1(Entity animal) {
        if (animal instanceof CraftAnimals) {
            referencedAnimal = animal;
            craftAnimal = (CraftAnimals) animal;
        }
    }

    @Override
    public RanchAnimal getRanchAnimal(Entity animal) {
        if (animal instanceof CraftAnimals) {
            return new RanchAnimal_V1_10_R1(animal);
        }
        return null;
    }

    @Override
    public int getAge() {
        return craftAnimal.getTicksLived();
    }

    @Override
    public boolean canBreed() {
        return craftAnimal.canBreed();
    }

    @Override
    public void setInLove() {
        if (craftAnimal.canBreed()) {
            EntityAnimal entAnimal = ((CraftAnimals) craftAnimal).getHandle();
            NBTTagCompound tag = new NBTTagCompound();
            entAnimal.b(tag);
            tag.setInt("InLove", 1600);
            entAnimal.a(tag);
        }
    }

    @Override
    public boolean isAdult() {
        return craftAnimal.isAdult();
    }

    // Sheep stuff
    @Override
    public boolean isSheered() {
        if (referencedAnimal.getType() == EntityType.SHEEP) {
            CraftSheep crftSheep = (CraftSheep) referencedAnimal;
            return crftSheep.isSheared();
        }
        return false;
    }

    @Override
    public boolean shearWool() {
        if (referencedAnimal.getType() == EntityType.SHEEP) {
            CraftSheep crftSheep = (CraftSheep) referencedAnimal;
            if (!crftSheep.isSheared()) {
                crftSheep.setSheared(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public double getHealth() {
        return craftAnimal.getHealth();
    }

    @Override
    public void damageAnimal(int damage) {
        craftAnimal.damage(damage);
    }

    @Override
    public DyeColor getColor() {
        if (referencedAnimal.getType() == EntityType.SHEEP) {
            CraftSheep crftSheep = (CraftSheep) referencedAnimal;
            return crftSheep.getColor();
        }
        return DyeColor.WHITE;
    }

    @Override
    public Material getWool() {
        return Material.WOOL;
    }

    @Override
    public void setTamed(boolean isTame) {
        if (!(referencedAnimal instanceof CraftTameableAnimal))
            return;
        CraftTameableAnimal oTamable = (CraftTameableAnimal) referencedAnimal;
        oTamable.setTamed(isTame);
    }

    @Override
    public Entity getEntity() {
        return referencedAnimal;
    }

    @Override
    public List<EntityType> getSupportedAnimals() {
        List<EntityType> supportedAnimals = new ArrayList<>();
        supportedAnimals.add(EntityType.COW);
        supportedAnimals.add(EntityType.CHICKEN);
        supportedAnimals.add(EntityType.SHEEP);
        supportedAnimals.add(EntityType.MUSHROOM_COW);
        supportedAnimals.add(EntityType.PIG);
        supportedAnimals.add(EntityType.HORSE);
        supportedAnimals.add(EntityType.RABBIT);
        supportedAnimals.add(EntityType.OCELOT);
        return supportedAnimals;
    }
}
