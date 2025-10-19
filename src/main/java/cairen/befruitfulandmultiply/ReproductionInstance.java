package cairen.befruitfulandmultiply;

import java.io.IOException;
import java.util.HashMap;

import script.SCRIPT.SCRIPT_INSTANCE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public class ReproductionInstance implements SCRIPT_INSTANCE {

    private final Reproduction reproduction;

    public ReproductionInstance(Reproduction reproduction) {
        this.reproduction = reproduction;
        reproduction.initData();
    }

    @Override
    public void load(FileGetter file) throws IOException {
        int size = file.i();
        HashMap<String, Double> loadedBirthHourTickers = new HashMap<>();
        for (int i = 0; i < size; i++) {
            loadedBirthHourTickers.put(file.chars(), file.d());
        }
        reproduction.birthHourTickers = loadedBirthHourTickers;
    }

    @Override
    public void save(FilePutter file) {
        file.i(reproduction.birthHourTickers.size());
        for (String key : reproduction.birthHourTickers.keySet()) {
            file.chars(key);
            file.d(reproduction.birthHourTickers.get(key));
        }
    }

    @Override
    public void update(double ds) {
        reproduction.update(ds);
    }


}
