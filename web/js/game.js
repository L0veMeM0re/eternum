window.ETERNUM = window.ETERNUM || {};

(function (E) {
  const rand = (a, b) => a + Math.floor(Math.random() * (b - a + 1));
  const chance = (pct) => Math.random() * 100 < pct;
  const pick = (arr) => arr[rand(0, arr.length - 1)];
  const clamp = (v, a, b) => Math.max(a, Math.min(b, v));

  function skillBag() {
    if (!E.state.skills || typeof E.state.skills !== "object") E.state.skills = {};
    const id = E.state.classId;
    if (!id) return {};
    if (!E.state.skills[id]) E.state.skills[id] = {};
    return E.state.skills[id];
  }

  function sk(id) {
    return skillBag()[id] || 0;
  }

  function detectSet(name) {
    const lower = String(name || "").toLowerCase();
    const found = (E.SETS || []).find((set) => !set.mythic && set.keys.some((key) => lower.includes(key)));
    return found ? found.id : "";
  }

  function tagOwnedItems() {
    const touch = (item) => {
      if (item) item.set = detectSet(item.name);
    };
    Object.values(E.state.equipment || {}).forEach(touch);
    (E.state.inventory || []).forEach(touch);
  }

  function setPieces(set) {
    const worn = Object.values(E.state.equipment || {}).filter(Boolean);
    if (set.mythic) return worn.filter((item) => item.rarity === "mythic").length;
    return worn.filter((item) => (item.set || detectSet(item.name)) === set.id).length;
  }

  function setReady(id, need) {
    const set = (E.SETS || []).find((entry) => entry.id === id);
    return !!set && setPieces(set) >= need;
  }

  function setStatBonus() {
    const bonus = { hp: 0, atk: 0, def: 0, crit: 0, critDmg: 0, dodge: 0, vamp: 0, vampHeal: 0 };
    (E.SETS || []).forEach((set) => {
      const count = setPieces(set);
      set.tiers.forEach((tier) => {
        if (count < tier.n) return;
        bonus.hp += tier.hp || 0;
        bonus.atk += tier.atk || 0;
        bonus.def += tier.def || 0;
        bonus.crit += tier.crit || 0;
        bonus.critDmg += tier.critDmg || 0;
        bonus.dodge += tier.dodge || 0;
        bonus.vamp += tier.vamp || 0;
        bonus.vampHeal = Math.max(bonus.vampHeal, tier.vampHeal || 0);
      });
    });
    return bonus;
  }

  E.detectSet = detectSet;
  E.setPieces = setPieces;
  E.setReady = setReady;
  E.skillOpen = function (which) { return sk(which === 1 ? "a1u" : "a2u") >= 1; };
  E.skillBonus = function (which) {
    const power = sk(which === 1 ? "a1p" : "a2p");
    const master = sk(which === 1 ? "a1m" : "a2m");
    return 1 + (power + master) * 0.05;
  };

  function blankSave() {
    return {
      prologueSeen: false,
      classId: null,
      level: 1,
      xp: 0,
      gold: 0,
      trainHp: 0,
      trainAtk: 0,
      expansions: 0,
      equipment: { weapon: null, helmet: null, armor: null, gloves: null, boots: null, ring: null, amulet: null },
      inventory: [],
      scrolls: { blessing: 0, xp: 0, luck: 0, greed: 0, fog: 0, breath: 0 },
      potions: 0,
      ascension: false,
      merc: null,
      autoSell: [],
      skills: {},
      meta: { crystals: 0, dmg: 0, hp: 0, drop: 0, bestFloor: 0, kills: 0, deaths: 0, goldEarned: 0, items: 0 }
    };
  }

  E.state = blankSave();
  E.run = null;

  E.load = function () {
    try {
      const raw = localStorage.getItem(E.SAVE_KEY);
      if (!raw) return;
      const data = JSON.parse(raw);
      E.state = Object.assign(blankSave(), data);
      E.state.equipment = Object.assign(blankSave().equipment, data.equipment || {});
      E.state.scrolls = Object.assign(blankSave().scrolls, data.scrolls || {});
      E.state.meta = Object.assign(blankSave().meta, data.meta || {});
      E.state.inventory = Array.isArray(data.inventory) ? data.inventory : [];
      E.state.skills = data.skills && typeof data.skills === "object" ? data.skills : {};
      tagOwnedItems();
      if (E.state.classId && !data.skillTreeInit) {
        const known = E.state.skills[E.state.classId] || {};
        known.a1u = Math.max(known.a1u || 0, 1);
        known.a2u = Math.max(known.a2u || 0, 1);
        E.state.skills[E.state.classId] = known;
        E.state.skillTreeInit = true;
        E.save();
      }
      if (!Array.isArray(E.state.autoSell) || !data.autoSellResetV3) {
        E.state.autoSell = [];
        E.state.autoSellResetV3 = true;
        E.save();
      }
    } catch (err) {
      E.state = blankSave();
    }
  };

  E.save = function () {
    localStorage.setItem(E.SAVE_KEY, JSON.stringify(E.state));
  };

  E.reset = function () {
    const seen = E.state.prologueSeen;
    E.state = blankSave();
    E.state.prologueSeen = seen;
    E.run = null;
    E.save();
  };

  E.invMax = function () {
    return 20 + E.state.expansions * 2;
  };

  E.zoneOf = function (floor) {
    return E.ZONES.find((z) => floor <= z.until) || E.ZONES[E.ZONES.length - 1];
  };

  E.bossOf = function (floor) {
    return E.BOSSES.find((b) => b.floor === floor) || null;
  };

  function rarityById(id) {
    return E.RARITIES.find((r) => r.id === id);
  }

  function equipTotals() {
    const t = { atk: 0, def: 0, hp: 0, crit: 0, critDmg: 0, dodge: 0, vamp: 0, vampHeal: 0 };
    Object.values(E.state.equipment).forEach((item) => {
      if (!item) return;
      t.atk += item.atk || 0;
      t.def += item.def || 0;
      t.hp += item.hp || 0;
      t.crit += item.crit || 0;
      t.critDmg += item.critDmg || 0;
      t.dodge += item.dodge || 0;
      t.vamp += item.vamp || 0;
      t.vampHeal = Math.max(t.vampHeal, item.vampHeal || 0);
    });
    t.vamp = Math.min(50, t.vamp);
    return t;
  }

  function runMods() {
    const mods = { dmg: 1, hp: 1, def: 1, crit: 0, dodge: 0, gold: 1, xp: 1, dropPen: 0, heal: 0, blessing: 0 };
    if (!E.run) return mods;
    mods.blessing = E.run.blessing || 0;
    mods.gold += E.run.greed || 0;
    mods.xp += E.run.xpScroll || 0;
    (E.run.effects || []).forEach((fx) => {
      mods.dmg += fx.dmg || 0;
      mods.hp += fx.hp || 0;
      mods.def += fx.def || 0;
      mods.crit += fx.crit || 0;
      mods.dodge += fx.dodge || 0;
      mods.gold += fx.gold || 0;
      mods.xp += fx.xp || 0;
      mods.dropPen += fx.drop || 0;
      mods.heal += fx.heal || 0;
    });
    mods.hp = Math.max(0.2, mods.hp);
    mods.dmg = Math.max(0.2, mods.dmg);
    mods.def = Math.max(0.2, mods.def);
    return mods;
  }

  E.heroSheet = function () {
    const cls = E.CLASSES[E.state.classId];
    if (!cls) return null;
    const eq = equipTotals();
    const mods = runMods();
    const meta = E.state.meta;
    const set = setStatBonus();
    let hp = cls.hp + (E.state.level - 1) * 5 + E.state.trainHp + eq.hp + set.hp;
    let atk = cls.atk + (E.state.level - 1) + E.state.trainAtk + eq.atk + set.atk;
    let def = cls.def + (E.state.level - 1) + eq.def + set.def;
    let crit = cls.crit + eq.crit + mods.crit + set.crit;
    let critDmg = cls.critDmg + eq.critDmg + set.critDmg;
    let dodge = cls.dodge + eq.dodge + mods.dodge + set.dodge;
    hp = Math.floor(hp * (1 + meta.hp * 0.05) * mods.hp);
    atk = Math.floor(atk * (1 + meta.dmg * 0.05) * (1 + mods.blessing) * mods.dmg);
    def = Math.floor(def * mods.def);
    if (cls.id === "CLERIC") hp = Math.floor(hp * 1.1);
    if (cls.id === "MAGE") critDmg += 0.4;
    if (cls.id === "ROGUE") crit += 8;
    const core = sk("core");
    const stat = sk("stat");
    if (cls.id === "WARRIOR") hp = Math.floor(hp * (1 + 0.035 * stat));
    if (cls.id === "MAGE") { critDmg += core * 0.05; crit += stat * 3; }
    if (cls.id === "ROGUE") { crit += core * 2; dodge += stat * 2; }
    if (cls.id === "CLERIC") {
      hp = Math.floor(hp * (1 + 0.02 * core));
      def += Math.floor(def * 0.02 * stat);
    }
    hp += sk("over") * 5;
    atk += sk("over");
    return {
      hp: Math.max(1, hp),
      atk: Math.max(1, atk),
      def: Math.max(0, def),
      crit: clamp(crit, 0, 50),
      critDmg,
      dodge: clamp(dodge, 0, 66),
      vamp: eq.vamp + set.vamp,
      vampHeal: Math.max(eq.vampHeal, set.vampHeal)
    };
  };

  E.mercSheet = function () {
    const merc = E.state.merc;
    if (!merc) return null;
    const cls = E.CLASSES[merc.classId];
    const lvl = merc.level;
    let hp = Math.floor((cls.hp + (lvl - 1) * 5) * 0.82);
    let atk = Math.floor((cls.atk + (lvl - 1)) * 0.82);
    let def = Math.floor((cls.def + (lvl - 1)) * 0.82);
    if (cls.id === "CLERIC") hp = Math.floor(hp * 1.1);
    const mods = runMods();
    hp = Math.floor(hp * mods.hp * (1 + E.state.meta.hp * 0.05));
    atk = Math.floor(atk * mods.dmg * (1 + mods.blessing) * (1 + E.state.meta.dmg * 0.05));
    return {
      hp: Math.max(1, hp),
      atk: Math.max(1, atk),
      def: Math.max(0, def),
      crit: clamp(cls.crit, 0, 50),
      critDmg: cls.critDmg + (cls.id === "MAGE" ? 0.4 : 0),
      dodge: clamp(cls.dodge, 0, 66)
    };
  };

  function xpNeed(level) {
    return 100 * level;
  }

  E.xpNeed = xpNeed;

  function grantXp(amount) {
    const notes = [];
    let xp = Math.max(0, Math.floor(amount));
    E.state.xp += xp;
    while (E.state.xp >= xpNeed(E.state.level)) {
      E.state.xp -= xpNeed(E.state.level);
      E.state.level += 1;
      notes.push("Кай достигает " + E.state.level + " уровня. Здоровье восстановлено. Очко умений ждёт в прокачке.");
    }
    if (E.state.merc) {
      E.state.merc.xp += xp;
      const cls = E.CLASSES[E.state.merc.classId];
      while (E.state.merc.xp >= xpNeed(E.state.merc.level)) {
        E.state.merc.xp -= xpNeed(E.state.merc.level);
        E.state.merc.level += 1;
        notes.push(E.state.merc.name + " (" + cls.name + ") достигает " + E.state.merc.level + " уровня.");
      }
    }
    return notes;
  }

  function difficultyMult(floor) {
    if (floor <= 10) return 1;
    if (floor <= 20) return 1.1;
    if (floor < 30) return 1.1 + (floor - 20) * 0.008;
    if (floor <= 40) return 1.22;
    if (floor <= 50) return 1.28;
    return 1.28 + (floor - 50) * 0.008;
  }

  function earlyEase(floor) {
    if (floor <= 10) return 0.8;
    if (floor <= 20) return 0.88;
    if (floor <= 30) return 0.94;
    return 1;
  }

  function scaleStat(base, floor, per, latePer) {
    const early = Math.min(floor, 50);
    const late = Math.max(0, floor - 50);
    return base + early * per + late * latePer;
  }

  function makeFighter(name, mark, stats, extra) {
    return Object.assign({
      name, mark, maxHp: stats.hp, hp: stats.hp, atk: stats.atk, def: stats.def,
      crit: stats.crit, critDmg: stats.critDmg, dodge: stats.dodge,
      elite: false, boss: false, alive: true,
      poison: 0, poisonDmg: 0, atkDown: 0, struck: false
    }, extra || {});
  }

  function scaleMob(typeId, floor, elite) {
    const type = E.MOBS[typeId];
    const mult = difficultyMult(floor) * (floor >= 80 ? 1.4 : 1) * earlyEase(floor);
    const hpPer = floor <= 30 ? 9 : 14;
    const atkPer = floor <= 30 ? 2 : 3;
    const defPer = floor <= 30 ? 1 : 2;
    let hp = Math.floor(scaleStat(type.hp, floor, hpPer, 22) * mult);
    let atk = Math.floor(scaleStat(type.atk, floor, atkPer, 5) * mult);
    let def = Math.floor(scaleStat(type.def, floor, defPer, 2) * mult);
    const crit = type.crit + floor * (floor <= 30 ? 0.1 : 0.15) + Math.max(0, floor - 50) * 0.4;
    const dodge = type.dodge + floor * (floor <= 30 ? 0.06 : 0.1) + Math.max(0, floor - 50) * 0.25;
    const critDmg = 1.8 + floor * 0.008 + Math.max(0, floor - 50) * 0.08;
    if (elite) {
      hp = Math.floor(hp * 1.5);
      atk = Math.floor(atk * 1.25);
      def = Math.floor(def * 1.25);
    }
    return { hp, atk, def, crit, critDmg, dodge };
  }

  function scaleBoss(floor) {
    const boss = E.bossOf(floor);
    const tier = Math.floor(floor / 10);
    let mult = difficultyMult(floor) * 1.05 * (floor >= 80 ? 1.4 : 1) * earlyEase(floor);
    if (floor >= 100) mult *= 1.3 * 1.3;
    else if (floor >= 90) mult *= 1.2;
    const hpPer = floor <= 30 ? 12 : 18;
    const hp = Math.floor(scaleStat(boss.hp + tier * 35, floor, hpPer, 35) * mult);
    const atk = Math.floor(scaleStat(boss.atk + tier * 3, floor, 3, 7) * mult);
    const def = Math.floor(scaleStat(boss.def + tier * 2, floor, 2, 3) * mult);
    return {
      hp, atk, def,
      crit: boss.crit + tier * 0.8 + Math.max(0, floor - 50) * 0.5,
      critDmg: 2.5 + tier * 0.08 + Math.max(0, floor - 50) * 0.12,
      dodge: boss.dodge + tier * 0.4 + Math.max(0, floor - 50) * 0.3
    };
  }

  function rollCount(floor) {
    if (floor < 10) return 1;
    if (floor < 20) return chance(30 + (floor - 10) * 7) ? 2 : 1;
    if (floor < 60) return chance(35 + (floor - 20) / 2) ? 3 : 2;
    if (floor < 80) return chance(35 + (floor - 60)) ? 4 : 3;
    return 4;
  }

  function fogHere() {
    return E.run && E.run.fog && E.run.floor <= 15;
  }

  function applyFog(fighter) {
    fighter.maxHp = Math.max(1, Math.round(fighter.maxHp * 0.72));
    fighter.hp = fighter.maxHp;
    fighter.atk = Math.max(1, Math.round(fighter.atk * 0.72));
    fighter.def = Math.max(0, Math.round(fighter.def * 0.72));
  }

  E.spawnEncounter = function () {
    E.run.floorKills = 0;
    E.run.berserkStand = false;
    E.run.swings = 0;
    const floor = E.run.floor;
    const zone = E.zoneOf(floor);
    const fog = fogHere();
    const foes = [];
    if (floor % 10 === 0) {
      const boss = E.bossOf(floor);
      const stats = scaleBoss(floor);
      const chief = makeFighter(boss.name, boss.mark, stats, { boss: true, art: "img/boss-" + floor + ".jpg" });
      if (fog) applyFog(chief);
      foes.push(chief);
      let minions = Math.max(1, rollCount(floor) - 1);
      if (fog) minions = Math.max(1, minions - 1);
      for (let i = 0; i < minions; i++) {
        const typeId = zone.pool[i % zone.pool.length];
        const type = E.MOBS[typeId];
        const mob = makeFighter("Слуга · " + type.name, type.mark, scaleMob(typeId, floor, false), { art: E.ART.mobs[typeId] });
        if (fog) applyFog(mob);
        foes.push(mob);
      }
    } else {
      let count = rollCount(floor);
      if (fog) count = Math.max(1, count - 1);
      const eliteSlot = floor >= 20 && chance(eliteChance(floor)) ? rand(0, count - 1) : -1;
      for (let i = 0; i < count; i++) {
        const typeId = pick(zone.pool);
        const type = E.MOBS[typeId];
        const elite = i === eliteSlot;
        const mob = makeFighter((elite ? "Элита · " : "") + type.name, type.mark, scaleMob(typeId, floor, elite), { elite, art: E.ART.mobs[typeId] });
        if (fog) applyFog(mob);
        foes.push(mob);
      }
    }
    E.run.foes = foes;
    E.run.selected = 0;
    E.run.between = false;
    E.run.log.push({ kind: "sys", text: "Этаж " + floor + " · " + zone.name + (foes.some((f) => f.boss) ? " · босс" : "") });
  };

  function eliteChance(floor) {
    if (floor < 20) return 0;
    if (floor < 70) return 5 + (floor - 20) * (9 / 50);
    if (floor >= 100) return 40;
    return 18 + (floor - 70) * (22 / 30);
  }

  function freshAllies() {
    const sheet = E.heroSheet();
    E.run.hero = {
      name: "Кай", mark: "К", classId: E.state.classId,
      maxHp: sheet.hp, hp: sheet.hp, atk: sheet.atk, def: sheet.def,
      crit: sheet.crit, critDmg: sheet.critDmg, dodge: sheet.dodge,
      vamp: sheet.vamp, vampHeal: sheet.vampHeal,
      cd1: 0, cd2: 0, defUp: 0, mad: 0, noCrit: 0, alive: true
    };
    const mercSheet = E.mercSheet();
    if (mercSheet && E.state.merc) {
      E.run.merc = {
        name: E.state.merc.name, classId: E.state.merc.classId,
        maxHp: mercSheet.hp, hp: mercSheet.hp, atk: mercSheet.atk, def: mercSheet.def,
        crit: mercSheet.crit, critDmg: mercSheet.critDmg, dodge: mercSheet.dodge, alive: true
      };
    } else {
      E.run.merc = null;
    }
  }

  function syncSheetsKeepingHp() {
    const prevH = E.run.hero.hp / Math.max(1, E.run.hero.maxHp);
    const sheet = E.heroSheet();
    E.run.hero.maxHp = sheet.hp;
    E.run.hero.hp = clamp(Math.round(sheet.hp * prevH), 1, sheet.hp);
    E.run.hero.atk = sheet.atk;
    E.run.hero.def = sheet.def;
    E.run.hero.crit = sheet.crit;
    E.run.hero.critDmg = sheet.critDmg;
    E.run.hero.dodge = sheet.dodge;
    E.run.hero.vamp = sheet.vamp;
    E.run.hero.vampHeal = sheet.vampHeal;
    if (E.run.merc && E.state.merc) {
      const prev = E.run.merc.hp / Math.max(1, E.run.merc.maxHp);
      const ms = E.mercSheet();
      E.run.merc.maxHp = ms.hp;
      E.run.merc.hp = E.run.merc.alive ? clamp(Math.round(ms.hp * prev), 0, ms.hp) : 0;
      E.run.merc.atk = ms.atk;
      E.run.merc.def = ms.def;
    }
  }

  E.startRun = function (floor) {
    const scrolls = E.state.scrolls;
    E.run = {
      floor: floor || 1,
      kills: 0,
      items: 0,
      peak: floor || 1,
      blessing: scrolls.blessing > 0 ? 0.15 : 0,
      xpScroll: scrolls.xp > 0 ? 0.5 : 0,
      luck: scrolls.luck > 0 ? 10 : 0,
      greed: scrolls.greed > 0 ? 0.25 : 0,
      fog: scrolls.fog > 0,
      breath: scrolls.breath > 0,
      breathUsed: false,
      potionsUsed: 0,
      effects: [],
      log: [],
      between: false,
      reward: null,
      finished: null,
      auto: true,
      beat: 0
    };
    if (scrolls.blessing > 0) scrolls.blessing--;
    if (scrolls.xp > 0) scrolls.xp--;
    if (scrolls.luck > 0) scrolls.luck--;
    if (scrolls.greed > 0) scrolls.greed--;
    if (scrolls.fog > 0) scrolls.fog--;
    if (scrolls.breath > 0) scrolls.breath--;
    freshAllies();
    const bits = [];
    if (E.run.blessing) bits.push("Благословение: +15% урона");
    if (E.run.xpScroll) bits.push("Свиток опыта: +50% XP");
    if (E.run.luck) bits.push("Свиток удачи: +10% дропа");
    if (E.run.greed) bits.push("Свиток жадности: +25% золота");
    if (E.run.fog) bits.push("Свиток тумана: этажи 1–15 слабее");
    if (E.run.breath) bits.push("Второе дыхание: одно воскрешение");
    if (bits.length) E.run.log.push({ kind: "good", text: bits.join(" · ") });
    E.spawnEncounter();
    E.save();
  };

  function livingFoes() {
    return E.run.foes.filter((f) => f.hp > 0);
  }

  function targetFoe() {
    const foes = livingFoes();
    if (!foes.length) return null;
    const chosen = E.run.foes[E.run.selected];
    if (chosen && chosen.hp > 0) return chosen;
    E.run.selected = E.run.foes.indexOf(foes[0]);
    return foes[0];
  }

  function log(kind, text, fx) {
    E.run.log.push({ kind, text, fx: fx || null, beat: E.run.beat || 0 });
    if (E.run.log.length > 80) E.run.log.shift();
  }

  function strike(att, defn, mods) {
    mods = mods || {};
    const hero = E.run && E.run.hero;
    if (!mods.ignoreDodge && chance(defn.dodge || 0)) {
      if (hero && defn === hero && setReady("shadow", 4)) defn.shadowNext = true;
      return { dmg: 0, crit: false, dodge: true };
    }
    let atkMult = (mods.atkMult || 1) * (att.mad ? 1.25 : 1) * berserkMult(att);
    if (hero && att === hero) {
      if (att.shadowNext) { atkMult *= 1.4; att.shadowNext = false; }
      if (att.ashLeft > 0) atkMult *= 1.15;
      if (setReady("dragon", 6) && att.hp / Math.max(1, att.maxHp) < 0.4) atkMult *= 1.2;
      if (setReady("dragon", 4) && !defn.hotBlood) { atkMult *= 1.25; defn.hotBlood = true; }
      if (setReady("abyss", 6)) {
        const missing = 1 - att.hp / Math.max(1, att.maxHp);
        atkMult *= 1 + Math.min(0.27, Math.floor(missing * 10) * 0.03);
      }
      if (setReady("chaos", 6)) atkMult *= 1 + Math.min(0.2, (E.run.floorKills || 0) * 0.02);
    }
    let defMult = (defn.defUp ? 1.18 : 1) * (defn.mad ? 0.83 : 1);
    if (hero && defn === hero && sk("cap") && defn.classId === "WARRIOR" && defn.hp / Math.max(1, defn.maxHp) < 0.3) defMult *= 1.2;
    const pen = mods.pen || 0;
    const eatk = Math.max(1, Math.floor((att.atk * (att.atkDown ? 0.8 : 1)) * atkMult));
    const edef = Math.max(0, Math.floor(defn.def * (1 - pen) * defMult));
    let base = Math.max(1, eatk - edef);
    const noCrit = mods.noCrit || defn.noCrit;
    const crit = !noCrit && chance((att.crit || 0) + (mods.critBonus || 0));
    let dmg = crit ? Math.floor(base * (att.critDmg || 1.5)) : base;
    dmg = Math.max(1, Math.floor(dmg * (mods.dmgMult || 1) * (mods.inMult || 1)));
    let echo = false;
    if (hero && att === hero && !mods.noEcho && setReady("chaos", 4) && chance(12)) {
      dmg += Math.max(1, Math.floor(dmg * 0.6));
      echo = true;
    }
    if (hero && defn === hero && setReady("titan", 6) && E.run.merc && E.run.merc.hp > 0) {
      const share = Math.floor(dmg * 0.15);
      dmg -= share;
      E.run.merc.hp = Math.max(0, E.run.merc.hp - share);
    }
    const next = defn.hp - dmg;
    if (next <= 0 && hero && defn === hero && spareHero(defn)) {
      return { dmg: defn.hp - 1, crit, dodge: false, echo, saved: true };
    }
    defn.hp = Math.max(0, next);
    if (defn.hp <= 0 && hero && att === hero) E.run.floorKills = (E.run.floorKills || 0) + 1;
    return { dmg, crit, dodge: false, echo };
  }

  function spareHero(hero) {
    if (sk("cap") && hero.classId === "BERSERKER" && !E.run.berserkStand) {
      E.run.berserkStand = true;
      hero.hp = 1;
      log("good", "Последний рубеж: Кай остаётся на 1 HP");
      return true;
    }
    if (setReady("phoenix", 6) && !E.run.phoenixAsh) {
      E.run.phoenixAsh = true;
      hero.hp = 1;
      hero.ashLeft = 4;
      log("good", "Пепел феникса: Кай вспыхивает с 1 HP");
      return true;
    }
    if (setReady("celestial", 6) && E.run.floor % 5 === 0 && E.run.aegisFloor !== E.run.floor) {
      E.run.aegisFloor = E.run.floor;
      hero.hp = 1;
      log("good", "Звёздный щит принимает удар");
      return true;
    }
    return false;
  }

  function heroStrike(hero, foe, mods) {
    const result = strike(hero, foe, mods);
    if (setReady("storm", 4)) {
      E.run.swings = (E.run.swings || 0) + 1;
      if (E.run.swings % 3 === 0) {
        const other = livingFoes().find((unit) => unit !== foe);
        if (other) {
          const echo = strike(hero, other, { atkMult: ((mods && mods.atkMult) || 1) * 0.5, noEcho: true });
          log("skill", "Разряд бури задевает " + other.name + " на " + echo.dmg, { actor: "Кай", target: other.name, dmg: echo.dmg });
        }
      }
    }
    if (setReady("storm", 6) && foe.hp <= 0 && chance(20)) {
      const other = livingFoes()[0];
      if (other) {
        const echo = strike(hero, other, { noEcho: true });
        log("skill", "Молния бьёт " + other.name + " на " + echo.dmg, { actor: "Кай", target: other.name, dmg: echo.dmg });
      }
    }
    return result;
  }

  function berserkMult(unit) {
    if (!unit || unit.classId !== "BERSERKER") return 1;
    const missing = 1 - unit.hp / Math.max(1, unit.maxHp);
    const stacks = Math.floor(missing / 0.05);
    const per = 0.0175 + sk("core") * 0.0025;
    const cap = 0.32 + sk("stat") * 0.04;
    return 1 + Math.min(cap, stacks * per);
  }

  function afterHeroHit(att, result) {
    if (result.dodge || result.dmg <= 0) return;
    if (att.classId === "ROGUE" && result.crit) {
      const heal = Math.max(1, Math.floor(att.maxHp * 0.05));
      att.hp = Math.min(att.maxHp, att.hp + heal);
      log("heal", att.name + " восстанавливает " + heal + " HP от крита");
    }
    if (att.vamp && att.vampHeal && chance(att.vamp)) {
      const heal = Math.max(1, Math.floor(result.dmg * att.vampHeal / 100));
      att.hp = Math.min(att.maxHp, att.hp + heal);
      log("heal", "Вампиризм: +" + heal + " HP");
    }
  }

  function describeHit(name, foeName, result, verb, actorName) {
    const actor = actorName || name;
    if (result.dodge) {
      log("miss", foeName + " уклоняется от " + name, { actor, target: foeName, dmg: 0 });
      return;
    }
    log(result.crit ? "crit" : "hit", name + " " + (verb || "бьёт") + " " + foeName + " на " + result.dmg + (result.crit ? " · крит" : ""), {
      actor, target: foeName, dmg: result.dmg, crit: result.crit
    });
  }

  function incomingGuard(defender, attacker) {
    let mult = 1;
    if (defender.classId === "WARRIOR" && attacker && !attacker.struck) {
      attacker.struck = true;
      mult *= 1 - (0.28 + sk("core") * 0.015);
    }
    if (E.run && defender === E.run.hero) {
      if (setReady("titan", 4)) mult *= 0.88;
      if (setReady("abyss", 4) && attacker && !attacker.abyssHit) {
        attacker.abyssHit = true;
        mult *= 0.7;
      }
    }
    return mult;
  }

  E.heroAct = function (action) {
    if (!E.run || E.run.between || E.run.finished) return;
    const hero = E.run.hero;
    if (hero.hp <= 0) {
      E.run.beat = (E.run.beat || 0) + 1;
      endOfHeroSide();
      return;
    }
    if (action === "potion") {
      drinkPotion();
      return;
    }
    if (action === "s1" && !E.skillOpen(1)) action = "attack";
    if (action === "s2" && !E.skillOpen(2)) action = "attack";
    if (action === "s1" && hero.cd1 > 0) return;
    if (action === "s2" && hero.cd2 > 0) return;
    E.run.beat = (E.run.beat || 0) + 1;
    if (hero.ashLeft > 0) hero.ashLeft--;

    const cls = E.CLASSES[hero.classId];
    const foe = targetFoe();
    const healRank = 0.2 + sk("a1p") * 0.015 + sk("a1m") * 0.005;
    if (action === "s1" && cls.id === "CLERIC") {
      castHeal(healRank * (sk("cap") ? 1.1 : 1));
      hero.cd1 = sk("a1m") >= 3 ? Math.max(1, cls.s1cd - 1) : cls.s1cd;
    } else if (action === "s2" && cls.id === "BERSERKER") {
      hero.mad = 3 + sk("a2m");
      hero.cd2 = cls.s2cd;
      log("skill", "Безумие: " + hero.mad + " хода двойных ударов");
    } else if (action === "s2" && cls.id === "WARRIOR") {
      if (hero.hp / hero.maxHp >= 0.4) {
        log("sys", "Непоколебимость доступна только при HP ниже 40%");
        return;
      }
      hero.maxHp = Math.floor(hero.maxHp * 1.18);
      hero.hp = Math.min(hero.maxHp, hero.hp + Math.floor(hero.maxHp * 0.18));
      hero.noCrit = 1 + sk("a2m");
      hero.cd2 = cls.s2cd;
      log("skill", "Непоколебимость: запас сил и иммунитет к криту");
    } else if (!foe && action !== "s1") {
      return;
    } else if (action === "s1" && cls.id === "MAGE") {
      livingFoes().forEach((f) => {
        const result = heroStrike(hero, f, { atkMult: 0.7 * E.skillBonus(1), pen: 0.5 + sk("a1m") * 0.05 });
        describeHit("Огненная волна", f.name, result, "сжигает", "Кай");
        afterHeroHit(hero, result);
      });
      hero.cd1 = cls.s1cd;
      if (sk("cap") && chance(10) && hero.cd2 === 0 && foe) {
        const extra = heroStrike(hero, foe, { atkMult: 1.2 * E.skillBonus(2) });
        describeHit(cls.s2, foe.name, extra, "сковывает", "Кай");
        foe.atkDown = 2 + sk("a2m");
        hero.cd2 = cls.s2cd;
        log("skill", "Перегрузка: ледяной оков следом");
      }
    } else if (action === "s1") {
      const mult = cls.id === "WARRIOR" ? 1.35 : cls.id === "ROGUE" ? 1.4 : cls.id === "BERSERKER" ? 1.65 : 1;
      const mods = { atkMult: mult * E.skillBonus(1) };
      if (cls.id === "ROGUE") {
        mods.ignoreDodge = true;
        mods.critBonus = 25 + sk("a1m") * 5;
        if (sk("cap") && foe.hp / foe.maxHp < 0.25) mods.atkMult *= 1.5;
      }
      if (cls.id === "BERSERKER") {
        const cost = Math.max(1, Math.floor(hero.hp * Math.max(0.04, 0.09 - sk("a1m") * 0.01)));
        hero.hp = Math.max(1, hero.hp - cost);
        log("bad", "Кровавый рывок забирает " + cost + " HP");
      }
      if (cls.id === "WARRIOR") hero.defUp = 2 + sk("a1m");
      const result = heroStrike(hero, foe, mods);
      describeHit(cls.s1, foe.name, result, "бьёт", "Кай");
      afterHeroHit(hero, result);
      hero.cd1 = cls.s1cd;
    } else if (action === "s2") {
      const mods = { atkMult: (cls.id === "MAGE" ? 1.2 : cls.id === "ROGUE" ? 1 : cls.id === "CLERIC" ? 1.1 : 1) * E.skillBonus(2) };
      const result = heroStrike(hero, foe, mods);
      describeHit(cls.s2, foe.name, result, "бьёт", "Кай");
      afterHeroHit(hero, result);
      if (cls.id === "MAGE") {
        foe.atkDown = 2 + sk("a2m");
        log("skill", foe.name + " скован льдом");
        if (sk("cap") && chance(10) && hero.cd1 === 0) {
          livingFoes().forEach((f) => {
            const extra = heroStrike(hero, f, { atkMult: 0.7 * E.skillBonus(1), pen: 0.5 + sk("a1m") * 0.05, noEcho: true });
            describeHit(cls.s1, f.name, extra, "сжигает", "Кай");
          });
          hero.cd1 = cls.s1cd;
          log("skill", "Перегрузка: огненная волна следом");
        }
      }
      if (cls.id === "ROGUE") {
        foe.poison = 3;
        foe.poisonDmg = Math.max(1, Math.floor(hero.atk * (0.3 + sk("a2m") * 0.1)));
        log("skill", foe.name + " отравлен");
      }
      if (cls.id === "CLERIC" && !result.dodge) {
        const steal = 0.5 + sk("a2m") * 0.1;
        const heal = Math.floor(result.dmg * steal * (sk("cap") ? 1.1 : 1));
        hero.hp = Math.min(hero.maxHp, hero.hp + heal);
        log("heal", "Святой удар возвращает " + heal + " HP");
      }
      hero.cd2 = cls.s2cd;
    } else {
      const times = hero.mad > 0 ? 2 : 1;
      for (let i = 0; i < times; i++) {
        const live = targetFoe();
        if (!live) break;
        const result = heroStrike(hero, live, {});
        describeHit("Кай", live.name, result, "атакует");
        afterHeroHit(hero, result);
      }
    }

    const fresh = {
      s1: action === "s1",
      s2: action === "s2",
      mad: action === "s2" && cls.id === "BERSERKER",
      def: action === "s1" && cls.id === "WARRIOR",
      nocrit: action === "s2" && cls.id === "WARRIOR"
    };
    endOfHeroSide();
    if (!E.run || E.run.finished) return;
    if (!fresh.mad && hero.mad > 0) hero.mad--;
    if (!fresh.def && hero.defUp > 0) hero.defUp--;
    if (!fresh.nocrit && hero.noCrit > 0) hero.noCrit--;
    if (!fresh.s1 && hero.cd1 > 0) hero.cd1--;
    if (!fresh.s2 && hero.cd2 > 0) hero.cd2--;
  };

  function castHeal(pct) {
    const units = [E.run.hero].concat(E.run.merc && E.run.merc.hp > 0 ? [E.run.merc] : []);
    units.sort((a, b) => a.hp / a.maxHp - b.hp / b.maxHp);
    const ally = units[0];
    const heal = Math.max(1, Math.floor(ally.maxHp * (pct || 0.2)));
    ally.hp = Math.min(ally.maxHp, ally.hp + heal);
    log("heal", "Исцеление: " + ally.name + " +" + heal + " HP");
  }

  function drinkPotion() {
    if (E.state.potions <= 0) {
      log("sys", "Зелий Абдолбос нет");
      return;
    }
    if (E.run.potionsUsed >= 10) {
      log("sys", "За этот забег больше нельзя пить зелья");
      return;
    }
    E.state.potions--;
    E.run.potionsUsed++;
    const good = chance(50);
    const pool = good ? E.POTION_GOOD : E.POTION_BAD;
    const fx = Object.assign({}, pick(pool));
    E.run.effects.push(fx);
    syncSheetsKeepingHp();
    log(good ? "good" : "bad", "Зелье Абдолбос: " + fx.label);
    E.save();
  }

  function endOfHeroSide() {
    if (livingFoes().length === 0) {
      victory();
      return;
    }
    enemyPhase();
    if (partyAlive()) {
      if (livingFoes().length === 0) victory();
    } else if (!tryBreath()) {
      defeat();
    }
  }

  function partyAlive() {
    return E.run.hero.hp > 0 || (E.run.merc && E.run.merc.hp > 0);
  }

  function enemyPhase() {
    E.run.foes.forEach((foe) => {
      if (foe.hp <= 0) return;
      if (foe.poison > 0) {
        foe.hp = Math.max(0, foe.hp - foe.poisonDmg);
        log("bad", foe.name + " теряет " + foe.poisonDmg + " HP от яда", { target: foe.name, dmg: foe.poisonDmg });
        if (setReady("shadow", 6) && E.run.hero.hp > 0) {
          const heal = Math.max(1, Math.floor(foe.poisonDmg * 0.3));
          E.run.hero.hp = Math.min(E.run.hero.maxHp, E.run.hero.hp + heal);
          log("heal", "Поглощение: +" + heal + " HP");
        }
        foe.poison--;
      }
    });
    E.run.foes.forEach((foe) => {
      if (foe.hp <= 0) return;
      const allies = [];
      if (E.run.hero.hp > 0) allies.push(E.run.hero);
      if (E.run.merc && E.run.merc.hp > 0) allies.push(E.run.merc);
      if (!allies.length) return;
      const ally = pick(allies);
      const result = strike(foe, ally, { inMult: incomingGuard(ally, foe) });
      describeHit(foe.name, ally.name, result, "ранит");
      if (foe.atkDown > 0) foe.atkDown--;
      if (ally.hp <= 0) {
        ally.alive = false;
        log("bad", ally.name + " падает");
      }
    });
    if (E.run.merc && E.run.merc.hp > 0 && livingFoes().length) {
      const foe = livingFoes().slice().sort((a, b) => a.hp - b.hp)[0];
      const result = strike(E.run.merc, foe, {});
      describeHit(E.run.merc.name, foe.name, result, "атакует");
    }
  }

  function tryBreath() {
    if (!E.run.breath || E.run.breathUsed) return false;
    E.run.breathUsed = true;
    const healPct = 0.25;
    E.run.hero.hp = Math.max(1, Math.floor(E.run.hero.maxHp * healPct));
    E.run.hero.alive = true;
    if (E.run.merc) {
      E.run.merc.hp = Math.max(1, Math.floor(E.run.merc.maxHp * healPct));
      E.run.merc.alive = true;
    }
    log("good", "Свиток второго дыхания поднимает отряд с 25% HP");
    return true;
  }

  function victory() {
    const floor = E.run.floor;
    let xp = 0;
    let gold = 0;
    const drops = [];
    E.run.foes.forEach((foe) => {
      if (foe.hp > 0) return;
      const rewardXp = Math.max(5, Math.floor(foe.maxHp / 3));
      const rewardGold = Math.max(3, foe.atk + Math.floor(foe.maxHp / 10));
      xp += foe.elite ? rewardXp * 2 : rewardXp;
      gold += foe.elite ? rewardGold * 2 : rewardGold;
      E.run.kills++;
      E.state.meta.kills++;
      const drop = foe.boss ? rollBossDrop(floor) : rollMobDrop(foe, floor);
      if (drop) drops.push(drop);
    });
    const mods = runMods();
    xp = Math.floor(xp * mods.xp * (setReady("celestial", 4) ? 1.1 : 1));
    gold = Math.floor(gold * mods.gold);
    E.state.gold += gold;
    E.state.meta.goldEarned += gold;
    const levelBefore = E.state.level;
    const notes = grantXp(xp);
    if (E.state.level !== levelBefore || (E.state.merc && notes.length)) syncSheetsKeepingHp();
    if (E.state.level !== levelBefore && E.run.hero.hp > 0) E.run.hero.hp = E.run.hero.maxHp;
    drops.forEach((item) => giveItem(item));
    if (setReady("phoenix", 4) && E.run.hero.hp > 0) {
      const spark = Math.max(1, Math.floor(E.run.hero.maxHp * 0.05));
      E.run.hero.hp = Math.min(E.run.hero.maxHp, E.run.hero.hp + spark);
      log("heal", "Искра феникса: +" + spark + " HP");
    }
    if (mods.heal > 0) {
      const heal = Math.max(1, Math.floor(E.run.hero.maxHp * mods.heal));
      E.run.hero.hp = Math.min(E.run.hero.maxHp, E.run.hero.hp + heal);
      log("heal", "Живучесть: +" + heal + " HP");
      if (E.run.merc && E.run.merc.hp > 0) {
        const mh = Math.max(1, Math.floor(E.run.merc.maxHp * mods.heal));
        E.run.merc.hp = Math.min(E.run.merc.maxHp, E.run.merc.hp + mh);
      }
    }
    E.run.peak = Math.max(E.run.peak, floor);
    E.run.between = true;
    E.run.reward = { xp, gold, notes, floor };
    log("good", "Победа. +" + xp + " XP, +" + gold + " золота");
    notes.forEach((n) => log("good", n));
    E.save();
  }

  E.nextFloor = function () {
    if (!E.run || !E.run.between) return;
    if (E.run.floor >= E.MAX_FLOOR) {
      finishRun(true);
      return;
    }
    E.run.floor++;
    E.run.peak = Math.max(E.run.peak, E.run.floor);
    syncSheetsKeepingHp();
    E.run.between = false;
    E.run.reward = null;
    E.spawnEncounter();
    E.save();
  };

  function finishRun(victory) {
    const floor = victory ? E.MAX_FLOOR : E.run.floor;
    E.state.meta.bestFloor = Math.max(E.state.meta.bestFloor, E.run.peak);
    let crystals = 8 + Math.floor((floor * 3) / 4) + Math.floor(E.run.kills / 20);
    if (victory) crystals += 20;
    if (!victory) E.state.meta.deaths++;
    E.state.meta.crystals += crystals;
    E.run.finished = { victory, floor, crystals, kills: E.run.kills, items: E.run.items };
    E.run.between = false;
    E.run.auto = false;
    log(victory ? "good" : "bad", victory ? "Башня пройдена." : "Забег оборвался на этаже " + floor + ".");
    log("good", "Кристаллы душ: +" + crystals);
    E.save();
  }

  function defeat() {
    finishRun(false);
  }

  E.retreat = function () {
    if (!E.run || E.run.finished) return;
    E.state.meta.bestFloor = Math.max(E.state.meta.bestFloor, E.run.peak);
    E.run.finished = { victory: false, retreat: true, floor: E.run.floor, crystals: 0, kills: E.run.kills, items: E.run.items };
    E.run.auto = false;
    E.run.between = false;
    log("sys", "Вы отступаете. Добыча при вас, кристаллы душ — нет.");
    E.save();
  };

  E.leaveRun = function () {
    E.run = null;
    E.save();
  };

  E.refreshCombat = function () {
    if (E.run && E.run.hero) syncSheetsKeepingHp();
  };

  function rollRarity(boss) {
    const roll = rand(1, 1000000);
    const mythic = boss ? 100 : 10;
    const legend = boss ? 5000 : 1000;
    const epic = boss ? 50000 : 30000;
    const rare = boss ? 200000 : 150000;
    if (roll <= mythic) return "mythic";
    if (roll <= mythic + legend) return "legendary";
    if (roll <= mythic + legend + epic) return "epic";
    if (roll <= mythic + legend + epic + rare) return "rare";
    return "common";
  }

  function bumpRarity(id, floor) {
    let idx = E.RARITIES.findIndex((r) => r.id === id);
    const steps = Math.floor(floor / 25);
    for (let i = 0; i < steps && idx < E.RARITIES.length - 1; i++) {
      if (chance(8)) idx++;
    }
    return E.RARITIES[idx].id;
  }

  function atLeast(id, minId) {
    return E.RARITIES.findIndex((r) => r.id === id) >= E.RARITIES.findIndex((r) => r.id === minId) ? id : minId;
  }

  function rollMobDrop(foe, floor) {
    const mods = runMods();
    let itemChance = 25 + E.state.meta.drop + (E.run.luck || 0) - Math.round(mods.dropPen * 100);
    itemChance = clamp(itemChance, 5, 35);
    if (chance(itemChance)) {
      let min = "common";
      if (foe.elite) min = floor >= 25 ? "epic" : "rare";
      else if (floor >= 40) min = "rare";
      return makeItem(floor, bumpRarity(atLeast(rollRarity(false), min), floor));
    }
    if (chance(28)) {
      let bonus = rand(3, 10) * (foe.elite ? 2 : 1);
      bonus = Math.floor(bonus * runMods().gold);
      E.state.gold += bonus;
      E.state.meta.goldEarned += bonus;
      log("gold", "+" + bonus + " бонусного золота");
    }
    return null;
  }

  function rollBossDrop(floor) {
    let min = "common";
    const bonus = E.state.meta.drop * 2;
    if (chance(Math.min(8, 2 + bonus / 10))) min = "legendary";
    else if (chance(Math.min(18, 6 + bonus / 5))) min = "epic";
    else if (chance(Math.min(35, 15 + bonus))) min = "rare";
    return makeItem(floor, bumpRarity(atLeast(rollRarity(true), min), floor));
  }

  function makeItem(floor, rarityId) {
    const rarity = rarityById(rarityId);
    const slot = pick(E.SLOTS).id;
    const tier = E.RARITIES.findIndex((r) => r.id === rarityId);
    const floorPart = 6 + Math.floor(floor / 4);
    const floors = { common: 4, rare: 12, epic: 22, legendary: 36, mythic: 52 };
    const base = Math.max(Math.floor(floorPart * rarity.mult), floors[rarityId] + Math.floor(floor / 12));
    const names = E.NAMES[slot][Math.min(tier, E.NAMES[slot].length - 1)];
    const catalog = E.SET_NAMES[slot] || {};
    const setIds = Object.keys(catalog);
    const wantSet = (tier === 1 && chance(55)) || tier >= 2;
    const item = {
      id: Date.now().toString(36) + Math.random().toString(36).slice(2, 7),
      name: wantSet && setIds.length ? catalog[pick(setIds)] : pick(names),
      slot,
      rarity: rarityId,
      atk: 0, def: 0, hp: 0, crit: 0, critDmg: 0, dodge: 0, vamp: 0, vampHeal: 0
    };
    if (slot === "weapon") {
      item.atk = base + tier * 3;
      item.crit = tier >= 1 ? tier * 1.7 : 0;
      item.critDmg = tier >= 2 ? tier * 0.15 : 0;
      item.dodge = tier >= 1 && chance(40) ? tier * 1.3 : 0;
    } else {
      item.def = slot === "armor" || slot === "helmet" ? base : Math.floor(base / 2);
      item.hp = slot === "armor" ? base : Math.floor(base / 3);
      item.atk = tier >= 2 && slot === "ring" ? Math.floor(base / 2) : 0;
      item.crit = slot === "amulet" ? tier * 2 : 0;
      item.critDmg = slot === "ring" ? tier * 0.12 : 0;
      item.dodge = slot === "boots" || slot === "gloves" ? tier * 1.7 : 0;
    }
    if (tier >= 1 && chance(8 + tier * 6)) {
      item.vamp = 3 + tier * 1.5;
      item.vampHeal = 12 + tier * 5;
    }
    const power = item.atk + item.def + Math.floor(item.hp / 4);
    item.sell = Math.max(2, Math.round(power * (1 + tier) + floor));
    item.set = detectSet(item.name);
    return item;
  }

  function noteLoot(item, how) {
    const now = Date.now();
    if (!E.lootPopup || now - E.lootPopup.started > 500) {
      E.lootPopup = { items: [], started: now, until: now + 3000 };
    }
    E.lootPopup.items.push({ item, how });
    E.lootPopup.until = E.lootPopup.started + 3000;
  }

  function giveItem(item) {
    E.run.items++;
    E.state.meta.items++;
    if (E.state.autoSell.includes(item.rarity)) {
      E.state.gold += item.sell;
      E.state.meta.goldEarned += item.sell;
      log("gold", "Автопродажа: " + item.name + " +" + item.sell);
      noteLoot(item, "sold");
      return;
    }
    if (E.state.inventory.length >= E.invMax()) {
      log("bad", "Инвентарь полон, " + item.name + " остаётся в пыли");
      noteLoot(item, "full");
      return;
    }
    E.state.inventory.push(item);
    log(item.rarity === "legendary" || item.rarity === "mythic" ? "loot" : "item", "Добыча: " + itemLabel(item));
    noteLoot(item, "keep");
  }

  E.itemLabel = itemLabel;
  function itemLabel(item) {
    const bits = [];
    if (item.atk) bits.push("АТК " + item.atk);
    if (item.def) bits.push("ЗЩ " + item.def);
    if (item.hp) bits.push("HP " + item.hp);
    if (item.crit) bits.push("крит " + trim(item.crit) + "%");
    if (item.critDmg) bits.push("крит-урон +" + trim(item.critDmg));
    if (item.dodge) bits.push("уклон " + trim(item.dodge) + "%");
    if (item.vamp) bits.push("вампиризм " + trim(item.vamp) + "%");
    const setId = item.set || detectSet(item.name);
    const set = (E.SETS || []).find((entry) => entry.id === setId);
    if (set) bits.unshift("сет " + set.name);
    if (item.rarity === "mythic") bits.unshift("Созвездие");
    return item.name + " · " + bits.join(", ");
  }

  function trim(n) {
    return Math.round(n * 10) / 10;
  }

  E.itemScore = function (item) {
    if (!item) return 0;
    return item.atk * 3 + item.def * 2 + item.hp * 0.45 + item.crit * 2 + item.dodge + item.critDmg * 8 + item.vamp;
  };

  E.equipItem = function (index) {
    const item = E.state.inventory[index];
    if (!item) return;
    const prev = E.state.equipment[item.slot];
    E.state.equipment[item.slot] = item;
    E.state.inventory.splice(index, 1);
    if (prev) E.state.inventory.push(prev);
    E.save();
  };

  E.unequip = function (slot) {
    const item = E.state.equipment[slot];
    if (!item) return;
    if (E.state.inventory.length >= E.invMax()) return "full";
    E.state.inventory.push(item);
    E.state.equipment[slot] = null;
    E.save();
  };

  E.equipBest = function () {
    let moved = 0;
    let guard = 0;
    while (guard++ < 40) {
      let best = null;
      E.state.inventory.forEach((item, index) => {
        const current = E.state.equipment[item.slot];
        if (E.itemScore(item) > E.itemScore(current) + 0.01) {
          if (!best || E.itemScore(item) - E.itemScore(current) > best.gain) {
            best = { index, gain: E.itemScore(item) - E.itemScore(current) };
          }
        }
      });
      if (!best) break;
      E.equipItem(best.index);
      moved++;
    }
    return moved;
  };

  E.sellIndex = function (index) {
    const item = E.state.inventory[index];
    if (!item) return;
    E.state.gold += item.sell;
    E.state.meta.goldEarned += item.sell;
    E.state.inventory.splice(index, 1);
    E.save();
  };

  E.sellRarity = function (rarity) {
    let gold = 0;
    let count = 0;
    E.state.inventory = E.state.inventory.filter((item) => {
      if (item.rarity !== rarity) return true;
      gold += item.sell;
      count++;
      return false;
    });
    E.state.gold += gold;
    E.state.meta.goldEarned += gold;
    E.save();
    return { count, gold };
  };

  E.sellAll = function () {
    let gold = 0;
    E.state.inventory.forEach((item) => { gold += item.sell; });
    const count = E.state.inventory.length;
    E.state.inventory = [];
    E.state.gold += gold;
    E.state.meta.goldEarned += gold;
    E.save();
    return { count, gold };
  };

  E.toggleAutoSell = function (rarity) {
    const list = E.state.autoSell;
    const i = list.indexOf(rarity);
    if (i >= 0) list.splice(i, 1);
    else list.push(rarity);
    E.save();
  };

  function spend(cost) {
    if (E.state.gold < cost) return false;
    E.state.gold -= cost;
    return true;
  }

  E.buy = function (kind) {
    const S = E.SHOP;
    const sc = E.state.scrolls;
    const fail = (msg) => ({ ok: false, msg });
    const ok = (msg) => { E.save(); return { ok: true, msg }; };
    if (kind === "blessing") {
      if (sc.blessing >= S.maxScrolls) return fail("Запас благословений полон");
      if (!spend(S.blessing)) return fail("Не хватает золота");
      sc.blessing++;
      return ok("Благословение спрятано в сумку");
    }
    if (kind === "xp") {
      if (sc.xp >= S.maxScrolls) return fail("Запас свитков опыта полон");
      if (!spend(S.xp)) return fail("Не хватает золота");
      sc.xp++;
      return ok("Свиток опыта шуршит в руках");
    }
    if (kind === "chest") {
      if (E.state.inventory.length >= E.invMax()) return fail("Сначала освободите рюкзак");
      if (!spend(S.chest)) return fail("Не хватает золота");
      const rarity = chance(3) ? "legendary" : chance(15) ? "epic" : chance(45) ? "rare" : "common";
      const item = makeItem(Math.max(10, E.state.level * 2), rarity);
      E.state.inventory.push(item);
      E.state.meta.items++;
      return ok("В сундуке: " + item.name);
    }
    if (kind === "hp") {
      if (E.state.trainHp / 10 >= S.maxTrain) return fail("Тело больше не принимает тренировки");
      if (!spend(S.trainHp)) return fail("Не хватает золота");
      E.state.trainHp += 10;
      return ok("+10 к запасу HP");
    }
    if (kind === "atk") {
      if (E.state.trainAtk >= S.maxTrain) return fail("Дальше рука не станет тяжелее");
      if (!spend(S.trainAtk)) return fail("Не хватает золота");
      E.state.trainAtk++;
      return ok("+1 к атаке");
    }
    if (kind === "bag") {
      if (E.state.expansions >= S.maxExpand) return fail("Рюкзак больше не растянуть");
      if (!spend(S.expand)) return fail("Не хватает золота");
      E.state.expansions++;
      return ok("В рюкзаке ещё две ячейки");
    }
    if (kind === "potion") {
      if (!spend(S.potion)) return fail("Не хватает золота");
      E.state.potions++;
      return ok("Зелье Абдолбос — риск и дар в одном глотке");
    }
    if (kind === "luck" || kind === "greed" || kind === "fog" || kind === "breath") {
      const cost = S[kind];
      if (sc[kind] >= S.maxScrolls) return fail("Таких свитков и так довольно");
      if (!spend(cost)) return fail("Не хватает золота");
      sc[kind]++;
      return ok("Свиток лёг к остальным");
    }
    if (kind === "key") {
      if (E.state.ascension) return fail("Ключ уже ваш");
      if (!spend(S.key)) return fail("Не хватает золота");
      E.state.ascension = true;
      return ok("Ключ возвышения открывает этаж 50");
    }
    return fail("Неизвестная покупка");
  };

  E.upgradeMeta = function (stat) {
    const meta = E.state.meta;
    const caps = { dmg: 20, hp: 20, drop: 10 };
    if (meta[stat] >= caps[stat]) return { ok: false, msg: "Потолок этого дара" };
    const cost = 5 + meta[stat] * 3 + (stat === "drop" ? 2 : 0);
    if (meta.crystals < cost) return { ok: false, msg: "Мало кристаллов душ" };
    meta.crystals -= cost;
    meta[stat]++;
    E.save();
    return { ok: true, msg: "Алтарь принимает кристалл" };
  };

  E.hireMerc = function (classId) {
    if (E.state.merc) return { ok: false, msg: "В отряде уже есть спутник" };
    if (!spend(E.SHOP.hire)) return { ok: false, msg: "Наём стоит 1000 золота" };
    const cls = E.CLASSES[classId];
    E.state.merc = {
      name: pick(E.MERC_NAMES),
      classId,
      level: Math.max(1, E.state.level - 1),
      xp: 0
    };
    E.save();
    return { ok: true, msg: E.state.merc.name + " встаёт рядом. " + cls.name + "." };
  };

  E.dismissMerc = function () {
    if (!E.state.merc) return;
    E.state.gold += 500;
    E.state.merc = null;
    E.save();
  };

  E.changeClass = function (classId) {
    if (!E.CLASSES[classId] || E.state.classId === classId) return;
    E.state.classId = classId;
    E.save();
  };

  E.chooseClass = function (classId) {
    E.state.classId = classId;
    E.state.level = 1;
    E.state.xp = 0;
    E.state.skillTreeInit = true;
    E.state.skills = {};
    E.save();
  };

  E.skillPoints = function () {
    let spent = 0;
    Object.values(E.state.skills || {}).forEach((tree) => {
      Object.values(tree || {}).forEach((rank) => { spent += rank || 0; });
    });
    return Math.max(0, (E.state.level || 1) - 1 - spent);
  };

  E.skillNodes = function (classId) {
    const cls = E.CLASSES[classId || E.state.classId];
    if (!cls) return [];
    const master = {
      WARRIOR: ["+1 ход защиты за ранг", "+1 ход иммунитета к криту за ранг"],
      MAGE: ["+5% игнора защиты за ранг", "+1 ход ледяного окова за ранг"],
      ROGUE: ["+5% крита финта за ранг", "+10% урона яда за ранг"],
      CLERIC: ["На 3 ранге перезарядка исцеления −1", "+10% лечения святого удара за ранг"],
      BERSERKER: ["−1% цены здоровья за ранг", "+1 ход безумия за ранг"]
    }[cls.id];
    const stat = {
      WARRIOR: "+3.5% максимального HP за ранг",
      MAGE: "+3% крита за ранг",
      ROGUE: "+2% уклона за ранг",
      CLERIC: "+2% защиты за ранг",
      BERSERKER: "+4% к потолку ярости за ранг"
    }[cls.id];
    const cap = {
      WARRIOR: ["Железная воля", "При HP ниже 30%: +20% защиты"],
      MAGE: ["Перегрузка", "10% шанс следом применить второй навык"],
      ROGUE: ["Казнь", "Финт по цели ниже 25% HP: ×1.5 урона"],
      CLERIC: ["Божественный свет", "+10% ко всему лечению"],
      BERSERKER: ["Последний рубеж", "Один раз за бой смертельный удар оставляет 1 HP"]
    }[cls.id];
    const nodes = [
      { id: "a1u", name: "Изучить: " + cls.s1, text: cls.s1text, max: 1, need: [] },
      { id: "a1p", name: cls.s1 + " — мощь", text: "+5% силы навыка за ранг", max: 3, need: ["a1u"] },
      { id: "a1m", name: cls.s1 + " — мастерство", text: master[0], max: 3, need: ["a1p"] },
      { id: "a2u", name: "Изучить: " + cls.s2, text: cls.s2text, max: 1, need: [] },
      { id: "a2p", name: cls.s2 + " — мощь", text: "+5% силы навыка за ранг", max: 3, need: ["a2u"] },
      { id: "a2m", name: cls.s2 + " — мастерство", text: master[1], max: 3, need: ["a2p"] },
      { id: "core", name: cls.passive, text: cls.passiveText, max: 3, need: [] },
      { id: "stat", name: "Усиление", text: stat, max: 3, need: ["core"] },
      { id: "cap", name: cap[0], text: cap[1], max: 1, need: ["a1m", "a2m"] }
    ];
    const tree = (E.state.skills || {})[cls.id] || {};
    const full = nodes.every((node) => (tree[node.id] || 0) >= node.max);
    if (full) nodes.push({ id: "over", name: "Запредельное", text: "+5 HP и +1 атаки за ранг. Можно качать дальше.", max: 99, need: ["cap"] });
    return nodes;
  };

  E.buySkill = function (nodeId) {
    const nodes = E.skillNodes();
    const node = nodes.find((entry) => entry.id === nodeId);
    if (!node) return { ok: false, msg: "Такого узла нет" };
    const tree = skillBag();
    const rank = tree[nodeId] || 0;
    if (rank >= node.max) return { ok: false, msg: "Узел уже на потолке" };
    const blocked = node.need.some((req) => {
      const need = nodeId === "cap" ? 1 : 1;
      return (tree[req] || 0) < need;
    });
    if (blocked) return { ok: false, msg: "Сначала предыдущий узел" };
    if (E.skillPoints() < 1) return { ok: false, msg: "Нет очков умений. Они приходят с уровнем." };
    tree[nodeId] = rank + 1;
    E.save();
    E.refreshCombat();
    return { ok: true, msg: node.name + " · ранг " + tree[nodeId] };
  };

  E.metaCost = function (stat) {
    return 5 + E.state.meta[stat] * 3 + (stat === "drop" ? 2 : 0);
  };

  E.pickAuto = function () {
    const hero = E.run.hero;
    const cls = E.CLASSES[hero.classId];
    const foes = livingFoes();
    const s1 = E.skillOpen(1);
    const s2 = E.skillOpen(2);
    if (s1 && cls.id === "CLERIC" && hero.cd1 === 0) {
      const units = [hero].concat(E.run.merc && E.run.merc.hp > 0 ? [E.run.merc] : []);
      if (units.some((u) => u.hp / u.maxHp < 0.55)) return "s1";
    }
    if (s2 && cls.id === "WARRIOR" && hero.cd2 === 0 && hero.hp / hero.maxHp < 0.4) return "s2";
    if (s2 && cls.id === "BERSERKER" && hero.cd2 === 0 && hero.hp / hero.maxHp < 0.55) return "s2";
    if (s1 && hero.cd1 === 0 && foes.length) return "s1";
    if (s2 && hero.cd2 === 0 && foes.length && cls.id !== "BERSERKER" && cls.id !== "WARRIOR") return "s2";
    return "attack";
  };
})(ETERNUM);
