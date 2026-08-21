package common.battle.data;

import common.CommonStatic;
import common.CommonStatic.BCAuxAssets;
import common.io.json.JsonClass;
import common.pack.Identifier;
import common.system.VImg;
import common.system.files.VFile;
import common.util.Data;
import common.util.anim.ImgCut;
import common.util.unit.Unit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.*;

@JsonClass
public class Orb extends Data {
	public static final byte[] orbTrait = {
			TRAIT_RED, TRAIT_FLOAT, TRAIT_BLACK, TRAIT_METAL, TRAIT_ANGEL, TRAIT_ALIEN,
			TRAIT_ZOMBIE, TRAIT_RELIC, TRAIT_WHITE, TRAIT_EVA, TRAIT_WITCH, TRAIT_DEMON, -1//This one is no trait anyway
	};
	/** Gets trait magnification data from BC.
	 * Format: Map< Type, Map< Grade, Effects > >*/
	public static final Map<Byte, Map<Byte, int[]>> EFFECT = new TreeMap<>();

	public static void read() {
		BCAuxAssets aux = CommonStatic.getBCAssets();
		try {
			Queue<String> traitData = VFile.readLine("./org/data/equipment_attribute.csv");

			byte key = 0;

			for (String line : traitData) {
				if (line == null || line.startsWith("//") || line.isEmpty())
					continue;

				String[] strs = line.trim().split(",");
				int value = 0;
				for (int i = 0; i < strs.length; i++) {
					if(strs.length != orbTrait.length)
						continue;

					int t = CommonStatic.parseIntN(strs[i]);
					if (t == 1)
						value |= 1 << orbTrait[i];
				}

				aux.DATA.put(key, value);
				key++;
			}

			String data = new String(VFile.get("./org/data/equipmentlist.json").getData().getBytes(), StandardCharsets.UTF_8);

			JSONObject jdata = new JSONObject(data);
			JSONArray lists = jdata.getJSONArray("ID");

			for (int i = 0; i < lists.length(); i++) {
				if (!(lists.get(i) instanceof JSONObject))
					continue;

				JSONObject obj = (JSONObject) lists.get(i);
				byte type = (byte)obj.getInt("content");
				byte grade = (byte)obj.getInt("gradeID");
				byte trait = obj.has("attribute") ? (byte)obj.getInt("attribute") : (byte)(aux.DATA.size() - 1);//No trait

				Map<Integer, List<Byte>> orb = aux.ORB.getOrDefault(type, new TreeMap<>());
				List<Byte> grades = orb.getOrDefault(aux.DATA.get(trait), new ArrayList<>());

				if(!grades.contains(grade))
					grades.add(grade);
				orb.put(aux.DATA.get(trait), grades);
				aux.ORB.put(type, orb);

				Map<Byte, int[]> effs = EFFECT.getOrDefault(type, new TreeMap<>());
				if (effs.containsKey(grade))
					continue;
				JSONArray value = obj.getJSONArray("value");
				int[] values = new int[value.length()];
				for (int j = 0; j < values.length; j++)
					values[j] = value.getInt(j);

				effs.put(grade, values);
				EFFECT.put(type, effs);
			}

			Queue<String> units = VFile.readLine("./org/data/equipmentslot.csv");

			for (String line : units) {
				if (line == null || line.startsWith("//") || line.isEmpty()) {
					continue;
				}

				String[] strs = line.trim().split(",");
				if (strs.length != 2 && strs.length != 2 + CommonStatic.parseIntN(strs[1]))
					continue;

				int id = CommonStatic.parseIntN(strs[0]);
				int slots = CommonStatic.parseIntN(strs[1]);

				Unit u = (Unit) Identifier.parseInt(id, Unit.class).get();

				if (u == null || u.forms.length < 3)
					continue;
				if (strs.length == 2) {
					for (int i = 0; i < slots; i++)
						u.orbs.add(new Orb(2, 0));
				} else
					for (int i = 0; i < slots; i++) {
						int limitId = CommonStatic.parseIntN(strs[2 + i]);
						int minForm = limitId >= 0 ? 2 : 0;
						int minLv = limitId * 60;//>= 1 ? 60 : 0;
						u.orbs.add(new Orb(minForm, minLv));
					}
			}

			String pre = "./org/page/orb/equipment_";
			VImg type = new VImg(pre + "effect.png");
			aux.TYPES[0] = ImgCut.newIns(pre + "effect.imgcut").cut(type.getImg());
			VImg typeS = new VImg(pre + "effect_s.png");
			aux.TYPES[1] = ImgCut.newIns(pre + "effect_s.imgcut").cut(typeS.getImg());

			VImg trait = new VImg(pre + "attribute.png");
			aux.TRAITS[0] = ImgCut.newIns(pre + "attribute.imgcut").cut(trait.getImg());
			VImg traitS = new VImg(pre + "attribute_s.png");
			aux.TRAITS[1] = ImgCut.newIns(pre + "attribute_s.imgcut").cut(traitS.getImg());

			VImg grade = new VImg(pre + "grade.png");
			aux.GRADES = ImgCut.newIns(pre + "grade.imgcut").cut(grade.getImg());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static int reverse(int value) {
		Map<Byte, Integer> DATA = CommonStatic.getBCAssets().DATA;
		for (byte n : DATA.keySet()) {
			int v = DATA.get(n);
			if (DATA.get(n) != null && v == value)
				return n;
		}
		return -1;
	}

	public static int traitToOrb(int trait) {
		for(int i = 0; i < orbTrait.length; i++) {
			if(orbTrait[i] == trait)
				return 1 << i;
		}
		return -1;
	}

	public static int[] get(byte type, byte grade) {
		return EFFECT.get(type).get(grade);
	}

	private static final int[] oneOnly = {ORB_DEATH_SURGE, ORB_REFUND, ORB_SOLBUFF, ORB_BAKILL,
			ORB_CANNON_CHARGE, ORB_DODGE, ORB_ULBUFF, ORB_COUNTERSURGE, ORB_KILLSTRENGTHEN, ORB_LESSCD};
	public static boolean onlyOne(int type) {
		for (int one : oneOnly)
			if (type == one)
				return true;
		return false;
	}
	public static int getAtk(int grade, int atk) {
		return get(ORB_ATK, (byte)grade)[0] * atk / 100;
	}

	public static int getRes(int grade, int atk) {
		return (100-get(ORB_RES,(byte)grade)[0]) * atk / 100;
	}

	public int minForm;
	public int minLv;

	@JsonClass.JCConstructor
	public Orb() {
	}

	public Orb(int minForm, int minLv) {
		this.minForm = minForm;
		this.minLv = minLv;
	}

	public boolean isRestricted(int formId, int lv) {
		return formId < minForm || lv < minLv;
	}

	@Override
	public String toString() {
		return "Form " + minForm + ", Lv. " + minLv;
	}
}