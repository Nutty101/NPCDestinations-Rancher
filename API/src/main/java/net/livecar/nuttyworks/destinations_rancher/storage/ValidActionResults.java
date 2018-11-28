package net.livecar.nuttyworks.destinations_rancher.storage;

import net.livecar.nuttyworks.destinations_rancher.animals.RanchAnimal;

import java.util.ArrayList;
import java.util.List;

public class ValidActionResults {
    public List<RanchAnimal> validAnimals;
    public RanchAnimal closest;
    public RanchAnimal breedMate;
    public Double distance;

    public ValidActionResults()
    {
        validAnimals = new ArrayList<RanchAnimal>();
        distance = 9999999D;
        closest = null;
        breedMate = null;
    }

}