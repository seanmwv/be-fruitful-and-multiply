package cairen.befruitfulandmultiply;

import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.type.*;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import view.sett.IDebugPanelSett;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Reproduction {

    // ...fill the earth and subdue it. Rule over the fish in the sea and the birds in the sky and over every living creature that moves on the ground.

    public static enum GENDER_STATUS {
        HERMAPHRODITIC,
        NONHERMAPHRODITIC
    }

    private HCLASS[] reproductiveHClasses;
    private HTYPE[] reproductiveHTypes;

    private final ArrayList<Race> reproductiveRaces;
    private final HashMap<String, GENDER_STATUS> raceReproductiveType;
    public HashMap<String, Double> birthHourTickers;

    private int hoursPerYear;
    private int lastHour;

    public Reproduction() {
        reproductiveRaces = new ArrayList<>();
        raceReproductiveType = new HashMap<>();
        birthHourTickers = new HashMap<>();
        lastHour = 0;

        initDebugPanel();
    }

    public void update(double ds) {
            int currentHour = TIME.hours().bitCurrent();
            if (lastHour != currentHour) {
                lastHour = currentHour;
                updateTickers();
            }
    }

    private void updateTickers() {
        for(Race race : reproductiveRaces) {
            for (HCLASS hclass : reproductiveHClasses) {
                HGROUP hgroup = HGROUP.get(hclass, race);
                int population = STATS.POP().POP.data(hclass).get(race);
                double growth = race.population().growth;
                GENDER_STATUS reproductiveType = raceReproductiveType.get(race.key);

                if (population <= 0 && reproductiveType == GENDER_STATUS.HERMAPHRODITIC ||
                population <= 1 && reproductiveType == GENDER_STATUS.NONHERMAPHRODITIC) { // WHATEVER
                    birthHourTickers.put(hgroup.key, 0.0);
                    continue;
                }

                double ticker = birthHourTickers.get(hgroup.key);
                double hourBirthInterval = getHourBirthInterval(growth, population);

                while (ticker >= hourBirthInterval) {
                    ticker -= hourBirthInterval;
                    RedeemBirth(hgroup);
                }

                birthHourTickers.put(hgroup.key, ++ticker);
            }
        }
    }

    public void initData() {
        int hoursPerDay = TIME.hoursPerDay;
        int daysPerSeason = TIME.days().bitsPerCycle();
        int seasonsPerYear = TIME.seasons().bitsPerCycle();
        hoursPerYear = hoursPerDay * daysPerSeason * seasonsPerYear;

        reproductiveHTypes = new HTYPE[]{
                HTYPES.SUBJECT(),
                HTYPES.SLAVE()
        };
        reproductiveHClasses = new HCLASS[]{
                HCLASSES.CITIZEN(),
                HCLASSES.SLAVE()
        };

        LIST<Race> allRaces = RACES.all();
        for (Race race : allRaces) {
            if (canReproduce(race)) {
                reproductiveRaces.add(race);
                if (isHermaphroditic(race)) {
                    raceReproductiveType.put(race.key, GENDER_STATUS.HERMAPHRODITIC);
                } else {
                    raceReproductiveType.put(race.key, GENDER_STATUS.NONHERMAPHRODITIC);
                }
            }

            for (HCLASS hclass : reproductiveHClasses) {
                HGROUP hgroup = HGROUP.get(hclass, race);
                birthHourTickers.put(hgroup.key, 0.0);
            }
        }
    }

    private double getYearlyBirthAmount(double growth, int population) {
        return growth * population; // ADD OTHER VARIABLES LATER
    }

    private double getHourBirthInterval(double growth, int population) {
        return population > 0 ? hoursPerYear / getYearlyBirthAmount(growth, population) : 0;
    }

    private boolean canReproduce(Race race) {
        return race.physics != null && race.physics.adultAt > 0;
    }

    private boolean isHermaphroditic(Race race) {
        int genderVariations = race.appearance().types.size();

        return genderVariations <= 1;
    }

    public LIST<Humanoid> getReproductiveMembers(HCLASS hclass, Race race) {
        ArrayListGrower<Humanoid> members = new ArrayListGrower<>();

        ENTITY[] allEntities = SETT.ENTITIES().getAllEnts();
        int maxIndex = SETT.ENTITIES().Imax();

        for (int i = 0; i <= maxIndex; i++) {
            ENTITY e = allEntities[i];

            if (!(e instanceof Humanoid)) {
                continue;
            }

            Humanoid h = (Humanoid) e;
            Induvidual ind = h.indu();

            if (ind.clas() != hclass || ind.race() != race) {
                continue;
            }

            if (!Arrays.asList(reproductiveHTypes).contains(ind.hType())) {
                continue;
            }

            members.add(h);
        }

        return members;
    }

    private void RedeemBirth(HGROUP hgroup) {
        LIST<Humanoid> parentalCandidates = getReproductiveMembers(hgroup.type, hgroup.race);

        int randIndex = RND.rInt(parentalCandidates.size() - 1);

        Humanoid parent = parentalCandidates.get(randIndex);

        int px = parent.physics.tileC().x(); // FIX
        int py = parent.physics.tileC().y(); // FIX
        HTYPE parentHtype = parent.indu().hType(); // FIX


        TrySpawnChildAt(hgroup.race, px, py, parentHtype);
    }

    public void TrySpawnChildAt(Race race, int tx, int ty, HTYPE parentHtype) {
        Humanoid baby = SETT.HUMANOIDS().create(race, tx, ty, parentHtype, CAUSE_ARRIVES.BORN());
        STATS.POP().age.DAYS.set(baby.indu(), race.physics.adultAt);
    }

    private void initDebugPanel() {
        IDebugPanelSett.add("Reproduction -- Print Data", new ACTION() {
            @Override
            public void exe() {
                System.out.println("REPRODUCTIVE STATS:");
                for(Race race : reproductiveRaces) {
                    for (HCLASS hclass : reproductiveHClasses){
                        HGROUP hgroup = HGROUP.get(hclass, race);
                        int population = STATS.POP().POP.data(hclass).get(race);
                        double growth = race.population().growth;
                        System.out.println("hgroup: " + hgroup.key);
                        System.out.println("race: " + race.key);
                        System.out.println("class: " + hclass.key);
                        System.out.println("population: " + population);
                        System.out.println("growth: " + growth);
                        System.out.println("reproductive type: " + raceReproductiveType.get(race.key));
                        System.out.println("hour birth interval: " + getHourBirthInterval(growth, population));
                        System.out.println("ticker: " + birthHourTickers.get(hgroup.key));
                    }
                }
            }
        });
        IDebugPanelSett.add("Reproduction -- Force Birth", new ACTION() {
            @Override
            public void exe() {
                for(Race race : reproductiveRaces) {
                    for (HCLASS hclass : reproductiveHClasses) {
                        HGROUP hgroup = HGROUP.get(hclass, race);
                        int population = STATS.POP().POP.data(hclass).get(race);

                        if (population > 0) {
                            RedeemBirth(hgroup);
                        }
                    }
                }
            }
        });
    }
}
