package ch.unibas.dmi.dbis.cs108.server.core.entities.artefacts;

public class ArtefactFactory {
    public static Artefact createArtefact(ArtefactData data) {
        if (data.getName() == null) {
            return null;
        }
        String name = data.getName();
        return switch (name) {
            case "Freyja's Necklace" -> new FreyjasNecklace(data);
            case "Freyr's Golden Apple" -> new FreyrsGoldenApple(data);
            case "Tear of Yggdrasil" -> new TearOfYggdrasil(data);
            case "Fragment of Mjölnir" -> new FragmentOfMjoelnir(data);
            case "Fenrir's Bones" -> new FenrirsBones(data);
            case "Hel's Shadow" -> new HelsShadow(data);
            case "Flame of Muspelheim" -> new FlameOfMuspelheim(data);
            case "Ice Splinter of Niflheim" -> new IceSplinterOfNiflheim(data);
            case "Blood of Jörmungandr" -> new BloodOfJoermungand(data);
            case "Ashes of Surtr" -> new AshesOfSurtr(data);
            case "Odin's Eye" -> new OdinsEye(data);
            case "Fenrir's Chains" -> new FenrirsChains(data);
            default -> throw new IllegalArgumentException("No matching artefact for name: " + name);
        };
    }
}