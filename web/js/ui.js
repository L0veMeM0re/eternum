(function (E) {
  const app = document.getElementById("app");
  let screen = "title";
  let townTab = "";
  let charTab = "gear";
  let wearWho = "kai";
  let treeFocus = "a1u";
  let prologueAt = 0;
  let autoTimer = null;

  const esc = (value) => String(value ?? "").replace(/[&<>"]/g, (ch) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;"
  }[ch]));

  function toast(text) {
    const node = document.getElementById("toast");
    node.textContent = text;
    node.hidden = false;
    clearTimeout(toast._t);
    toast._t = setTimeout(() => { node.hidden = true; }, 2400);
  }

  function show(next) {
    if (next !== "combat") pauseTimer();
    screen = next;
    render();
  }

  function bar(current, max) {
    const pct = Math.max(0, Math.min(100, Math.round((current / Math.max(1, max)) * 100)));
    const tone = pct < 30 ? "low" : pct < 60 ? "mid" : "high";
    return `<div class="bar ${tone}"><span style="width:${pct}%"></span></div>`;
  }

  function rarityName(id) {
    return (E.RARITIES.find((r) => r.id === id) || {}).name || id;
  }

  function slotName(id) {
    return (E.SLOTS.find((s) => s.id === id) || {}).name || id;
  }

  function shell(inner, extra) {
    if (extra === "combat-wrap") return `<div class="wrap combat-wrap">${inner}</div>`;
    return `<div class="shell">${dock()}<main class="stage">${inner}</main></div>`;
  }

  function dock() {
    const cls = E.CLASSES[E.state.classId];
    if (!cls) return "";
    const item = (id, label, icon) => `<button class="${screen === id ? "on" : ""}" data-act="${id}"><img src="${icon}" alt=""><span>${label}</span></button>`;
    return `<aside class="dock">
      <div class="dock-hero">
        <img src="${E.ART.kai}" alt="">
        <div>
          <b>Кай</b>
          <span>${esc(cls.name)} · ${E.state.level} ур.</span>
        </div>
      </div>
      <div class="dock-res">
        <div><img src="${E.ART.gold}" alt="">${E.state.gold}</div>
        <div><img src="${E.ART.soul}" alt="">${E.state.meta.crystals}</div>
      </div>
      <nav>
        ${item("hub", "Меню", E.ART.title)}
        ${item("character", E.skillPoints() ? "Герой · " + E.skillPoints() : "Герой", E.ART.classes[cls.id])}
        ${item("town", "Площадь", E.ART.town)}
        ${item("world", "Мир", E.ART.zones.forest)}
        ${item("stats", "Память", E.ART.scroll)}
      </nav>
    </aside>`;
  }

  function render() {
    if (screen === "title") app.innerHTML = renderTitle();
    else if (screen === "prologue") app.innerHTML = renderPrologue();
    else if (screen === "class") app.innerHTML = renderClass();
    else if (screen === "hub") app.innerHTML = shell(renderHub());
    else if (screen === "character") app.innerHTML = shell(renderCharacter());
    else if (screen === "town") app.innerHTML = shell(renderTown());
    else if (screen === "world") app.innerHTML = shell(renderWorld());
    else if (screen === "stats") app.innerHTML = shell(renderStats());
    else if (screen === "combat") app.innerHTML = shell(renderCombat(), "combat-wrap");
    const log = app.querySelector(".log");
    if (log) log.scrollTop = log.scrollHeight;
    showLoot();
    if (screen === "combat" && E.run && E.run.auto && !E.run.finished && !autoTimer) startAuto();
  }

  function showLoot() {
    let node = document.getElementById("loot-pop");
    if (!node) {
      node = document.createElement("div");
      node.id = "loot-pop";
      node.hidden = true;
      document.body.appendChild(node);
    }
    const pop = E.lootPopup;
    if (!pop || !pop.items.length || Date.now() >= pop.until) {
      node.hidden = true;
      return;
    }
    const key = String(pop.started);
    if (node.dataset.key !== key) {
      const rarityName = (id) => (E.RARITIES.find((r) => r.id === id) || {}).name || id;
      const cards = pop.items.map(({ item, how }) => {
        const note = how === "sold" ? "Сразу продано за " + item.sell + " зол." : how === "full" ? "Рюкзак полон" : "В рюкзаке";
        return `<article class="loot-card rarity-${item.rarity}">
          <img src="${E.ART.slots[item.slot] || E.ART.chest}" alt="">
          <div>
            <em>${esc(rarityName(item.rarity))}</em>
            <b>${esc(item.name)}</b>
            <small>${esc(E.itemLabel(item).split(" · ").slice(1).join(" · "))}</small>
            <span>${note}</span>
          </div>
        </article>`;
      }).join("");
      node.innerHTML = `<div class="loot-window"><p>Добыча</p>${cards}</div>`;
      node.dataset.key = key;
      node.hidden = false;
      clearTimeout(showLoot._t);
      showLoot._t = setTimeout(() => { node.hidden = true; }, Math.max(0, pop.until - Date.now()));
    }
  }

  function renderTitle() {
    const cont = E.state.classId;
    return `<section class="title-screen">
      <div class="title-copy">
      <p class="eyebrow">Серебряный Брод · Башня Молчания</p>
      <h1>Этернум</h1>
      <p class="tagline">Ты умрёшь — и мир скажет, что богов больше нет.<br>Только голос в голове и чужое лицо в воде.</p>
      <div class="title-actions">
        ${cont ? `<button class="btn solid" data-act="continue">Продолжить</button>` : `<button class="btn solid" data-act="begin">Начать</button>`}
        <button class="btn line" data-act="prologue">${E.state.prologueSeen ? "Перечитать пролог" : "Пролог"}</button>
        ${cont ? `<button class="btn line danger" data-act="wipe">Новая история</button>` : ""}
      </div>
      <p class="fine">100 этажей. Пять классов. Смерть оставляет кристаллы душ.</p>
      </div>
    </section>`;
  }

  function renderPrologue() {
    const scene = E.PROLOGUE[prologueAt];
    const art = prologueAt < 3 ? E.ART.title : prologueAt < 6 ? E.ART.kai : E.ART.title;
    return `<section class="prologue">
      <img class="prologue-art" src="${art}" alt="">
      <div class="scene">
        <p class="eyebrow">${esc(scene.kicker)} · ${prologueAt + 1} / ${E.PROLOGUE.length}</p>
        <h2>${esc(scene.title)}</h2>
        <div class="prose">${esc(scene.text).replace(/\n/g, "<br>")}</div>
        <div class="row">
          ${prologueAt > 0 ? `<button class="btn line" data-act="pro-prev">Назад</button>` : `<span></span>`}
          <button class="btn line" data-act="skip-pro">Пропустить</button>
          <button class="btn solid" data-act="pro-next">${prologueAt === E.PROLOGUE.length - 1 ? "К выбору пути" : "Дальше"}</button>
        </div>
      </div>
    </section>`;
  }

  function renderClass() {
    const cards = Object.values(E.CLASSES).map((cls) => `<article class="class-card" style="--hue:${cls.hue}">
      <img class="class-art" src="${E.ART.classes[cls.id]}" alt="">
      <h3>${esc(cls.name)}</h3>
      <p>${esc(cls.blurb)}</p>
      <ul>
        <li>HP ${cls.hp} · АТК ${cls.atk} · ЗЩ ${cls.def}</li>
        <li>Крит ${cls.crit}% · уклон ${cls.dodge}%</li>
      </ul>
      <p class="skill"><b>${esc(cls.passive)}.</b> ${esc(cls.passiveText)}</p>
      <p class="skill"><b>${esc(cls.s1)}.</b> ${esc(cls.s1text)}</p>
      <p class="skill"><b>${esc(cls.s2)}.</b> ${esc(cls.s2text)}</p>
      <button class="btn solid" data-act="pick-class" data-arg="${cls.id}">${E.state.classId ? "Сменить" : "Выбрать"}</button>
    </article>`).join("");
    return `<section class="class-screen">
      <div class="section-head">
        <p class="eyebrow">Пробуждённый</p>
        <h2>${E.state.classId ? "Смена класса" : "Кем ты войдёшь в башню"}</h2>
        <p>Уровень, золото и рюкзак останутся. Меняется только то, как Кай держит удар.</p>
      </div>
      <div class="class-grid">${cards}</div>
      ${E.state.classId ? `<button class="btn line back" data-act="character">Вернуться</button>` : ""}
    </section>`;
  }

  function renderHub() {
    const cls = E.CLASSES[E.state.classId];
    const sheet = E.heroSheet();
    const meta = E.state.meta;
    const merc = E.state.merc;
    const active = E.run && !E.run.finished;
    return `<section class="hub">
      <div class="hero-panel">
        <div class="portrait-stack">
          <img class="portrait" src="${E.ART.kai}" alt="Кай">
          <img class="class-badge" src="${E.ART.classes[cls.id]}" alt="${esc(cls.name)}">
        </div>
        <div>
          <p class="eyebrow">Кай · ${esc(cls.name)}</p>
          <h2>Серебряный Брод ещё не спит</h2>
          <p class="lead">Лиру увезли в Башню Молчания. Окраина без костров. Интерфейс виден только тебе.</p>
          <div class="meters">
            <div><span>Опыт</span>${bar(E.state.xp, E.xpNeed(E.state.level))}<em>${E.state.xp} / ${E.xpNeed(E.state.level)}</em></div>
            <div><span>Лучший этаж</span><strong>${meta.bestFloor || "—"} / ${E.MAX_FLOOR}</strong></div>
          </div>
        </div>
      </div>
      <div class="stat-row">
        ${stat("Здоровье", sheet.hp)}
        ${stat("Атака", sheet.atk)}
        ${stat("Защита", sheet.def)}
        ${stat("Крит", Math.round(sheet.crit) + "%")}
        ${stat("Уклон", Math.round(sheet.dodge) + "%")}
        ${stat("Крит-урон", "×" + sheet.critDmg.toFixed(1))}
      </div>
      <div class="menu-grid">
        <button class="menu-card" data-act="${active ? "combat" : "launch"}">
          <img src="${E.ART.slots.weapon}" alt="">
          <b>${active ? "Вернуться в башню" : "Новый забег"}</b>
          <span>${active ? "Этаж " + E.run.floor + " ещё идёт сам" : "Бой идёт сам, от этажа к этажу."}</span>
        </button>
        <button class="menu-card" data-act="character"><img src="${E.ART.classes[cls.id]}" alt=""><b>Персонаж</b><span>Рюкзак, экипировка, класс</span></button>
        <button class="menu-card" data-act="char-tab" data-arg="tree"><img src="${E.ART.scroll}" alt=""><b>Прокачка</b><span>${E.skillPoints()} очк. умений · навыки и пассивка</span></button>
        <button class="menu-card" data-act="char-tab" data-arg="sets"><img src="${E.ART.chest}" alt=""><b>Сеты</b><span>Дракон, тень, буря, бездна и остальные</span></button>
        <button class="menu-card" data-act="town"><img src="${E.ART.town}" alt=""><b>Площадь</b><span>Лавки, зелья, алтарь душ, наёмник</span></button>
        <button class="menu-card" data-act="world"><img src="${E.ART.zones.forest}" alt=""><b>Мир</b><span>Лес, пещера, болото, руины, бездна</span></button>
        <button class="menu-card" data-act="stats"><img src="${E.ART.scroll}" alt=""><b>Память</b><span>${meta.kills} убийств · ${meta.deaths} смертей</span></button>
      </div>
      <aside class="side-note">
        <p><b>Отряд.</b> ${merc ? esc(merc.name) + ", " + esc(E.CLASSES[merc.classId].name) + ", ур. " + merc.level : "Ты один. На площади можно нанять спутника."}</p>
        <p><b>Пассивка.</b> ${esc(cls.passive)} — ${esc(cls.passiveText)}</p>
      </aside>
    </section>`;
  }

  function stat(label, value) {
    return `<div class="stat"><span>${label}</span><strong>${value}</strong></div>`;
  }

  function wearTarget() {
    if (wearWho === "merc" && E.state.merc && E.state.merc.equipment) return "merc";
    return "kai";
  }

  function renderCharacter() {
    const who = wearTarget();
    const merc = E.state.merc;
    const gear = who === "merc" ? merc.equipment : E.state.equipment;
    const sheet = who === "merc" ? E.mercSheet() : E.heroSheet();
    const cls = E.CLASSES[who === "merc" ? merc.classId : E.state.classId];
    const slots = E.SLOTS.map((slot) => {
      const item = gear[slot.id];
      return `<button class="slot ${item ? "filled rarity-" + item.rarity : ""}" data-act="unequip" data-arg="${slot.id}">
        <img class="slot-icon" src="${E.ART.slots[slot.id]}" alt="">
        <span class="slot-copy">
          <em>${esc(slot.name)}</em>
          <b>${item ? esc(item.name) : "Свободно"}</b>
          <small>${item ? esc(E.itemLabel(item).split(" · ").slice(1).join(" · ")) : "нет вещи"}</small>
        </span>
      </button>`;
    }).join("");
    const bag = E.state.inventory.length
      ? E.state.inventory.map((item, i) => `<li class="item rarity-${item.rarity}">
          <img class="slot-icon" src="${E.ART.slots[item.slot]}" alt="">
          <div><b>${esc(item.name)}</b><small>${esc(rarityName(item.rarity))} · ${esc(slotName(item.slot))} · ${esc(E.itemLabel(item).split(" · ").slice(1).join(" · "))}</small></div>
          <div class="row tight">
            <button data-act="equip" data-arg="${i}">${who === "merc" ? "Спутнику" : "Надеть"}</button>
            <button data-act="sell" data-arg="${i}">${item.sell} зол.</button>
          </div>
        </li>`).join("")
      : `<li class="empty">Рюкзак пуст. Башня это исправит.</li>`;
    const autos = E.RARITIES.map((r) => `<button class="chip ${E.state.autoSell.includes(r.id) ? "on" : ""}" data-act="autosell" data-arg="${r.id}">${esc(r.name)}</button>`).join("");
    const tabs = ["gear", "tree", "sets"].map((id) => {
      const label = id === "gear" ? "Снаряжение" : id === "tree" ? "Прокачка" + (E.skillPoints() ? " · " + E.skillPoints() : "") : "Сеты";
      return `<button class="chip ${charTab === id ? "on" : ""}" data-act="char-tab" data-arg="${id}">${label}</button>`;
    }).join("");
    const activeSets = (E.SETS || []).map((set) => {
      const count = E.setPieces(set, gear);
      return count ? set.name + " " + count : "";
    }).filter(Boolean).join(" · ");
    const wearChips = merc
      ? `<div class="chips">
          <button class="chip ${who === "kai" ? "on" : ""}" data-act="wear" data-arg="kai">Кай</button>
          <button class="chip ${who === "merc" ? "on" : ""}" data-act="wear" data-arg="merc">${esc(merc.name)}</button>
        </div>`
      : "";
    const body = charTab === "tree" ? renderTree() : charTab === "sets" ? renderSets(gear) : `
      ${wearChips}
      <div class="row">
        <button class="btn solid" data-act="equip-best">${who === "merc" ? "Лучшее спутнику" : "Надеть лучшее"}</button>
        <button class="btn line" data-act="sell-common">Продать обычное</button>
        <button class="btn line" data-act="sell-all">Продать всё</button>
        <button class="btn line" data-act="class">Сменить класс</button>
        ${E.run ? `<button class="btn line" data-act="combat">К бою</button>` : ""}
      </div>
      <h3>${who === "merc" ? "Снаряжение спутника" : "Экипировка"}</h3>
      <div class="slots">${slots}</div>
      <h3>Рюкзак ${E.state.inventory.length}/${E.invMax()}</h3>
      <p class="fine">Автопродажа при подборе:</p>
      <div class="chips">${autos}</div>
      <ul class="bag">${bag}</ul>`;
    return `<section class="character">
      <div class="section-head with-face">
        <img class="face" src="${who === "merc" ? E.ART.classes[merc.classId] : E.ART.kai}" alt="">
        <div>
        <p class="eyebrow">${esc(cls.name)} · уровень ${who === "merc" ? merc.level : E.state.level}</p>
        <h2>${who === "merc" ? esc(merc.name) : "Кай"}</h2>
        <p>${sheet.hp} HP · ${sheet.atk} атаки · ${sheet.def} защиты · крит ${Math.round(sheet.crit)}% · уклон ${Math.round(sheet.dodge)}%</p>
        <p class="fine">${activeSets || "Сет ещё не собран. Редкие вещи носят имя сета."}</p>
        </div>
      </div>
      <div class="chips">${tabs}</div>
      ${body}
    </section>`;
  }

  function renderTree() {
    const cls = E.CLASSES[E.state.classId];
    const tree = (E.state.skills || {})[E.state.classId] || {};
    const nodes = E.skillNodes();
    const byId = Object.fromEntries(nodes.map((node) => [node.id, node]));
    const points = E.skillPoints();
    const spots = {
      a1u: [18, 80], a1p: [18, 56], a1m: [18, 32],
      a2u: [50, 80], a2p: [50, 56], a2m: [50, 32],
      core: [82, 68], stat: [82, 44],
      cap: [34, 14]
    };
    const short = {
      a1u: cls.s1, a1p: "Мощь", a1m: "Мастерство",
      a2u: cls.s2, a2p: "Мощь", a2m: "Мастерство",
      core: cls.passive, stat: "Усиление",
      cap: (byId.cap || {}).name || "Венец",
      over: "Запредельное"
    };
    const branchOf = {
      a1u: "s1", a1p: "s1", a1m: "s1",
      a2u: "s2", a2p: "s2", a2m: "s2",
      core: "pass", stat: "pass",
      cap: "cap", over: "cap"
    };
    const markOf = { s1: "I", s2: "II", pass: "✦", cap: "★" };
    if (!byId[treeFocus]) treeFocus = "a1u";
    const stateOf = (node) => {
      const rank = tree[node.id] || 0;
      const locked = node.need.some((req) => (tree[req] || 0) < 1);
      const done = rank >= node.max;
      return { rank, locked, done, ready: !locked && !done && points > 0 };
    };
    const wires = [
      ["a1u", "a1p"], ["a1p", "a1m"], ["a1m", "cap"],
      ["a2u", "a2p"], ["a2p", "a2m"], ["a2m", "cap"],
      ["core", "stat"]
    ].map(([from, to]) => {
      const [x1, y1] = spots[from];
      const [x2, y2] = spots[to];
      const bend = Math.abs(x2 - x1) < 2 ? 0 : (x1 < x2 ? -6 : 6);
      const live = (tree[from] || 0) > 0;
      return `<path class="${live ? "live" : ""}" d="M ${x1} ${y1} Q ${(x1 + x2) / 2 + bend} ${(y1 + y2) / 2} ${x2} ${y2}"></path>`;
    }).join("");
    const gems = nodes.filter((node) => spots[node.id]).map((node) => {
      const [x, y] = spots[node.id];
      const st = stateOf(node);
      const branch = branchOf[node.id];
      const pips = node.max > 3
        ? `<em>${st.rank}</em>`
        : Array.from({ length: node.max }, (_, i) => `<i class="${i < st.rank ? "lit" : ""}"></i>`).join("");
      return `<button type="button" class="gem b-${branch} ${st.done ? "done" : ""} ${st.locked ? "locked" : ""} ${st.ready ? "ready" : ""} ${treeFocus === node.id ? "focus" : ""}" data-act="tree-focus" data-arg="${node.id}" style="left:${x}%;top:${y}%">
        <span class="gem-ring"><em>${markOf[branch]}</em></span>
        <b>${esc(short[node.id] || node.name)}</b>
        <span class="pips">${pips}</span>
      </button>`;
    }).join("");
    const focus = byId[treeFocus];
    const focusState = stateOf(focus);
    const needText = focus.need.length
      ? "Открывается после: " + focus.need.map((id) => short[id] || id).join(" и ") + "."
      : "Можно учить сразу.";
    const over = byId.over;
    const overBlock = over ? (() => {
      const st = stateOf(over);
      return `<div class="talent-over ${st.done ? "done" : ""}">
        <div><b>Запредельное</b><span>Ранг ${st.rank}. ${esc(over.text)}</span></div>
        <button class="btn solid" data-act="buy-skill" data-arg="over" ${st.ready ? "" : "disabled"}>${points ? "Вложить очко" : "Нет очков"}</button>
      </div>`;
    })() : "";
    return `<div class="talent" style="--hue:${cls.hue}">
      <div class="talent-top">
        <div>
          <p class="eyebrow">${esc(cls.name)}</p>
          <h3>Дерево умений</h3>
        </div>
        <div class="talent-points"><b>${points}</b><span>${pointWord(points)}</span></div>
      </div>
      <div class="talent-board">
        <svg class="talent-wires" viewBox="0 0 100 100" preserveAspectRatio="none">${wires}</svg>
        ${gems}
        <span class="trunk" style="left:18%">Навык I</span>
        <span class="trunk" style="left:50%">Навык II</span>
        <span class="trunk" style="left:82%">Пассивка</span>
      </div>
      <article class="talent-detail ${focusState.done ? "done" : ""} ${focusState.locked ? "locked" : ""}">
        <div>
          <p class="eyebrow">${esc(short[focus.id] || focus.name)} · ранг ${focusState.rank}/${focus.max === 99 ? "∞" : focus.max}</p>
          <h3>${esc(focus.name)}</h3>
          <p>${esc(focus.text)}</p>
          <p class="fine">${focusState.locked ? esc(needText) : focusState.done ? "Узел заполнен." : "Повторный клик по узлу тоже вкладывает очко."}</p>
        </div>
        <button class="btn solid" data-act="buy-skill" data-arg="${focus.id}" ${focusState.ready ? "" : "disabled"}>${focusState.done ? "Изучено" : focusState.locked ? "Закрыто" : points ? "Вложить очко" : "Нет очков"}</button>
      </article>
      ${overBlock}
    </div>`;
  }

  function pointWord(n) {
    const n10 = n % 10;
    const n100 = n % 100;
    if (n10 === 1 && n100 !== 11) return "очко";
    if (n10 >= 2 && n10 <= 4 && (n100 < 12 || n100 > 14)) return "очка";
    return "очков";
  }

  function renderSets(gear) {
    const cards = E.SETS.map((set) => {
      const count = E.setPieces(set, gear);
      const max = set.tiers[set.tiers.length - 1].n;
      const tiers = set.tiers.map((tier) => `<li class="${count >= tier.n ? "on" : ""}"><b>${tier.n} шт.</b> ${esc(tier.text)}</li>`).join("");
      return `<article class="set-card ${count >= 2 ? "live" : ""}">
        <header><b>${esc(set.name)}</b><span>${count}/${max}</span></header>
        <ul>${tiers}</ul>
      </article>`;
    }).join("");
    return `<div class="tree-head">
      <h3>Сеты</h3>
      <p>Редкие и выше вещи почти всегда из сета. Бонус включается от надетых частей: 2, 4 и 6. Мифические ещё копят Созвездие.</p>
    </div>
    <div class="set-grid">${cards}</div>`;
  }

  function renderTown() {
    const places = [
      ["gold", "Лавка", E.ART.gold, "Свитки, сундук и тренировки"],
      ["magic", "Магия", E.ART.potion, "Зелья и свитки на забег"],
      ["altar", "Алтарь", E.ART.soul, "Кристаллы душ"],
      ["tavern", "Таверна", E.ART.classes.ROGUE, "Наём в отряд"]
    ];
    const pins = places.map(([id, label, icon, hint]) => `<button class="place ${townTab === id ? "on" : ""}" data-act="town-tab" data-arg="${id}">
      <img src="${icon}" alt="">
      <b>${label}</b>
      <span>${hint}</span>
    </button>`).join("");
    const panel = townTab ? `<section class="shop-panel">
      <div class="shop-head">
        <h2>${places.find((p) => p[0] === townTab)[1]}</h2>
        <button class="btn line" data-act="town-tab" data-arg="">Закрыть</button>
      </div>
      ${townTab === "gold" ? townGold() : townTab === "magic" ? townMagic() : townTab === "altar" ? townAltar() : townTavern()}
    </section>` : `<p class="map-hint">Нажми здание на площади — откроется лавка.</p>`;
    return `<section class="town">
      <div class="town-stage">
        <img class="town-bg" src="${E.ART.town}" alt="Площадь Серебряного Брода">
        <div class="places">${pins}</div>
      </div>
      ${panel}
    </section>`;
  }

  function offer(act, title, text, price) {
    const icons = {
      blessing: E.ART.scroll, xp: E.ART.scroll, chest: E.ART.chest,
      hp: E.ART.classes.WARRIOR, atk: E.ART.slots.weapon, bag: E.ART.chest,
      potion: E.ART.potion, luck: E.ART.scroll, greed: E.ART.gold,
      fog: E.ART.scroll, breath: E.ART.soul, key: E.ART.soul
    };
    return `<article class="offer">
      <img class="offer-icon" src="${icons[act] || E.ART.gold}" alt="">
      <div><b>${title}</b><p>${text}</p></div>
      <button class="btn solid" data-act="buy" data-arg="${act}">${price}</button>
    </article>`;
  }

  function townGold() {
    const S = E.SHOP;
    const sc = E.state.scrolls;
    return `<div class="offers">
      ${offer("blessing", "Благословение", "+15% урона на забег. В запасе " + sc.blessing + "/" + S.maxScrolls, S.blessing + " зол.")}
      ${offer("xp", "Свиток опыта", "+50% опыта на забег. В запасе " + sc.xp + "/" + S.maxScrolls, S.xp + " зол.")}
      ${offer("chest", "Закрытый сундук", "Случайная вещь по уровню героя.", S.chest + " зол.")}
      ${offer("hp", "Тренировка тела", "+10 HP навсегда. Куплено " + (E.state.trainHp / 10) + "/" + S.maxTrain, S.trainHp + " зол.")}
      ${offer("atk", "Тренировка руки", "+1 атаки навсегда. Куплено " + E.state.trainAtk + "/" + S.maxTrain, S.trainAtk + " зол.")}
      ${offer("bag", "Расширить рюкзак", "+2 ячейки. Сейчас " + E.invMax() + ". Расширений " + E.state.expansions + "/" + S.maxExpand, S.expand + " зол.")}
    </div>`;
  }

  function townMagic() {
    const S = E.SHOP;
    const sc = E.state.scrolls;
    return `<div class="offers">
      ${offer("potion", "Зелье Абдолбос", "В бою: случайный дар или проклятие до конца забега. Не больше десяти глотков. Есть " + E.state.potions, S.potion + " зол.")}
      ${offer("luck", "Свиток удачи", "+10% к шансу вещи. В запасе " + sc.luck + "/" + S.maxScrolls, S.luck + " зол.")}
      ${offer("greed", "Свиток жадности", "+25% золота на забег. В запасе " + sc.greed + "/" + S.maxScrolls, S.greed + " зол.")}
      ${offer("fog", "Свиток тумана", "Этажи 1–15: враги слабее и их меньше. В запасе " + sc.fog + "/" + S.maxScrolls, S.fog + " зол.")}
      ${offer("breath", "Второе дыхание", "Один раз поднять отряд с 25% HP. В запасе " + sc.breath + "/" + S.maxScrolls, S.breath + " зол.")}
      ${offer("key", "Ключ возвышения", E.state.ascension ? "Уже у тебя. Можно начать с 50 этажа." : "Открывает старт забега с 50 этажа.", E.state.ascension ? "получен" : S.key + " зол.")}
    </div>`;
  }

  function townAltar() {
    const meta = E.state.meta;
    const row = (stat, title, text, cap) => `<article class="offer">
      <img class="offer-icon" src="${E.ART.soul}" alt="">
      <div><b>${title}</b><p>${text} Уровень ${meta[stat]}/${cap}. Сейчас ${stat === "drop" ? "+" + meta.drop + "% дропа" : "+" + meta[stat] * 5 + "%"}.</p></div>
      <button class="btn solid" data-act="meta" data-arg="${stat}">${meta[stat] >= cap ? "потолок" : E.metaCost(stat) + " душ"}</button>
    </article>`;
    return `<div class="offers">
      ${row("dmg", "Остриё души", "Навсегда повышает урон.", 20)}
      ${row("hp", "Сосуд души", "Навсегда повышает здоровье.", 20)}
      ${row("drop", "Чутьё падальщика", "Чаще находишь вещи.", 10)}
    </div>`;
  }

  function townTavern() {
    if (E.state.merc) {
      const m = E.state.merc;
      const cls = E.CLASSES[m.classId];
      return `<article class="offer">
        <img class="offer-icon" src="${E.ART.classes[m.classId]}" alt="">
        <div><b>${esc(m.name)}</b><p>${esc(cls.name)}, уровень ${m.level}. Дерётся сам, опыт делит с тобой. Отпустить — вернуть 500 золота.</p></div>
        <button class="btn line danger" data-act="dismiss">Отпустить</button>
      </article>`;
    }
    const cards = Object.values(E.CLASSES).map((cls) => `<button class="class-mini" data-act="hire" data-arg="${cls.id}">
      <img src="${E.ART.classes[cls.id]}" alt="">
      <b>${esc(cls.name)}</b><span>${esc(cls.blurb)}</span>
    </button>`).join("");
    return `<p class="lead">Один спутник за ${E.SHOP.hire} золота. Он слабее героя, но бьёт каждый ход.</p><div class="hire-grid">${cards}</div>`;
  }

  function renderWorld() {
    const best = E.state.meta.bestFloor || 0;
    const cards = E.ZONES.map((zone, index) => {
      const from = index === 0 ? 1 : E.ZONES[index - 1].until + 1;
      const here = best >= from && best <= zone.until;
      const mobs = zone.pool.map((id) => `<div class="mob-pill">
          <img src="${E.ART.mobs[id]}" alt="">
          <span>${esc(E.MOBS[id].name)}</span>
        </div>`).join("");
      const bosses = E.BOSSES.filter((b) => b.floor >= from && b.floor <= zone.until)
        .map((b) => `<figure class="boss-card">
          <img src="img/boss-${b.floor}.jpg" alt="">
          <figcaption><b>${b.floor}</b>${esc(b.name)}</figcaption>
        </figure>`).join("");
      return `<article class="tower-band ${here ? "is-here" : ""}">
        <img class="tower-scene" src="${E.ART.zones[zone.tone]}" alt="">
        <div class="tower-copy">
          <p class="eyebrow">Этажи ${from}–${zone.until}${here ? " · ты был здесь" : ""}</p>
          <h3>${esc(zone.name)}</h3>
          <p>${esc(zone.text)}</p>
          <div class="mob-row">${mobs}</div>
          <div class="boss-row">${bosses}</div>
        </div>
      </article>`;
    }).join("");
    return `<section class="world">
      <div class="section-head">
        <p class="eyebrow">Карта башни</p>
        <h2>Пять поясов</h2>
        <p>Снизу лес, наверху бездна. На каждом десятом этаже — хозяин пояса.</p>
      </div>
      <div class="tower-list">${cards}</div>
      <button class="btn solid" data-act="launch">Войти в башню</button>
    </section>`;
  }

  function renderStats() {
    const m = E.state.meta;
    return `<section class="stats">
      <div class="section-head">
        <p class="eyebrow">Интерфейс</p>
        <h2>Что уже записано</h2>
      </div>
      <div class="stat-row">
        ${stat("Лучший этаж", m.bestFloor)}
        ${stat("Убийства", m.kills)}
        ${stat("Смерти", m.deaths)}
        ${stat("Золото за всё время", m.goldEarned)}
        ${stat("Найденные вещи", m.items)}
        ${stat("Кристаллы душ", m.crystals)}
      </div>
      <p class="lead">Сохранение лежит в этом браузере. Чтобы выложить игру, достаточно папки web: любой статический хостинг открывает index.html.</p>
      <button class="btn line danger" data-act="wipe">Стереть историю</button>
    </section>`;
  }

  function renderCombat() {
    const run = E.run;
    if (!run) return `<section class="combat"><p>Забег уже закрыт.</p><button class="btn solid" data-act="hub">В меню</button></section>`;
    const zone = E.zoneOf(run.floor);
    const cls = E.CLASSES[E.state.classId];
    const hero = run.hero;
    const done = run.finished;
    const swings = new Set();
    const hurts = new Map();
    run.log.forEach((line) => {
      if (!line.fx || line.beat !== run.beat) return;
      if (line.fx.actor) swings.add(line.fx.actor);
      if (line.fx.target && line.fx.dmg) hurts.set(line.fx.target, line.fx);
    });
    const mark = (name) => `${swings.has(name) ? " is-strike" : ""}${hurts.has(name) ? " is-hurt" : ""}`;
    const floater = (name) => {
      const hit = hurts.get(name);
      if (!hit) return "";
      return `<em class="float-dmg${hit.crit ? " crit" : ""}">−${hit.dmg}</em>`;
    };
    const foes = run.foes.map((foe, i) => `<button class="foe ${foe.hp <= 0 ? "is-dead" : ""} ${foe.boss ? "is-boss" : ""} ${run.selected === i ? "is-selected" : ""}${mark(foe.name)}" data-act="target" data-arg="${i}">
      <span class="art-wrap">
        <img class="foe-art" src="${foe.art || E.ART.mobs.GOBLIN}" alt="">
        ${floater(foe.name)}
      </span>
      <b>${esc(foe.name)}</b>
      ${bar(foe.hp, foe.maxHp)}
      <small>${Math.max(0, foe.hp)} / ${foe.maxHp} · АТК ${foe.atk} · ЗЩ ${foe.def}</small>
    </button>`).join("");
    const log = run.log.map((line) => `<p class="lg-${line.kind}">${esc(line.text)}</p>`).join("");
    const reward = run.reward;
    let foot = "";
    if (done) {
      foot = `<div class="result ${done.victory ? "win" : ""}">
        <h3>${done.victory ? "Башня замолчала" : done.retreat ? "Отступление" : "Кай падает"}</h3>
        <p>${done.victory ? "Все сто этажей пройдены." : "Этаж " + done.floor + "."} Убийств: ${done.kills}. Вещей: ${done.items}. ${done.crystals ? "Души +" + done.crystals + "." : "Кристаллы душ не получены."}</p>
        <button class="btn solid" data-act="end-run">Вернуться в брод</button>
      </div>`;
    } else if (run.between) {
      foot = `<div class="result">
        <h3>Этаж ${reward.floor} чист</h3>
        <p>+${reward.xp} опыта, +${reward.gold} золота. ${run.floor >= E.MAX_FLOOR ? "Дальше только трон тьмы позади." : "Дальше — этаж " + (run.floor + 1) + "."}</p>
        <div class="row">
          <button class="btn solid" data-act="next-floor">${run.floor >= E.MAX_FLOOR ? "Завершить" : "Дальше"}</button>
          <button class="btn line" data-act="equip-best">Надеть лучшее</button>
          <button class="btn line" data-act="character">Рюкзак</button>
          <button class="btn line" data-act="toggle-auto">${run.auto ? "Пауза" : "Автобой"}</button>
        </div>
      </div>`;
    } else {
      foot = `<div class="actions">
        <button class="btn line" data-act="fight" data-arg="potion"><img class="ico" src="${E.ART.potion}" alt="">Зелье · ${E.state.potions}</button>
        <button class="btn line" data-act="toggle-auto">${run.auto ? "Пауза" : "Автобой"}</button>
        <button class="btn line" data-act="character">Рюкзак</button>
        <button class="btn line danger" data-act="retreat">Отступить</button>
      </div>
      <p class="fine">${run.auto ? "Автобой. Умения срабатывают сами." : "Пауза."}</p>`;
    }
    const merc = run.merc;
    return `<section class="combat" style="--scene:url('${E.ART.zones[zone.tone]}')">
      <header class="combat-head">
        <div>
          <p class="eyebrow">${esc(zone.name)} · ${run.floor} / ${E.MAX_FLOOR}${run.fog && run.floor <= 15 ? " · туман" : ""}</p>
          <h2>${run.foes.some((f) => f.boss && f.hp > 0) ? esc(run.foes[0].name) : "Этаж " + run.floor}</h2>
        </div>
        ${bar(run.floor, E.MAX_FLOOR)}
      </header>
      <div class="battle">
        <div class="allies">
          <article class="unit${mark("Кай")}">
            <span class="art-wrap">
              <img class="unit-art" src="${E.ART.classes[cls.id]}" alt="">
              ${floater("Кай")}
            </span>
            <b>Кай · ${esc(cls.name)}</b>
            ${bar(hero.hp, hero.maxHp)}
            <small>${hero.hp} / ${hero.maxHp} · АТК ${hero.atk} · ЗЩ ${hero.def}</small>
          </article>
          ${merc ? `<article class="unit ${merc.hp <= 0 ? "is-dead" : ""}${mark(merc.name)}">
            <span class="art-wrap">
              <img class="unit-art" src="${E.ART.classes[merc.classId]}" alt="">
              ${floater(merc.name)}
            </span>
            <b>${esc(merc.name)} · ${esc(E.CLASSES[merc.classId].name)}</b>
            ${bar(merc.hp, merc.maxHp)}
            <small>${Math.max(0, merc.hp)} / ${merc.maxHp}</small>
          </article>` : ""}
        </div>
        <div class="log">${log}</div>
        <div class="foes">${foes}</div>
      </div>
      ${foot}
    </section>`;
  }

  function launch() {
    if (E.run && !E.run.finished) {
      show("combat");
      return;
    }
    if (E.run && E.run.finished) E.leaveRun();
    const start = (floor) => {
      E.startRun(floor);
      show("combat");
    };
    if (E.state.ascension) {
      const fromFifty = confirm("Ключ возвышения при тебе. Начать с 50 этажа?\nОтмена — с первого.");
      start(fromFifty ? 50 : 1);
      return;
    }
    start(1);
  }

  function finishPrologue() {
    E.state.prologueSeen = true;
    E.save();
    show(E.state.classId ? "hub" : "class");
  }

  function onClick(event) {
    const node = event.target.closest("[data-act]");
    if (!node || node.disabled) return;
    const act = node.dataset.act;
    const arg = node.dataset.arg;
    if (act === "begin") {
      prologueAt = 0;
      show(E.state.prologueSeen ? (E.state.classId ? "hub" : "class") : "prologue");
    } else if (act === "continue") show("hub");
    else if (act === "prologue") { prologueAt = 0; show("prologue"); }
    else if (act === "skip-pro" || act === "pro-next") {
      if (act === "pro-next" && prologueAt < E.PROLOGUE.length - 1) { prologueAt++; render(); }
      else finishPrologue();
    } else if (act === "pro-prev") { prologueAt = Math.max(0, prologueAt - 1); render(); }
    else if (act === "hub") show("hub");
    else if (act === "character") { charTab = "gear"; show("character"); }
    else if (act === "char-tab") { charTab = arg || "gear"; show("character"); }
    else if (act === "tree-focus") {
      if (treeFocus === arg && node.classList.contains("ready")) {
        const res = E.buySkill(arg);
        toast(res.msg);
        E.refreshCombat();
      } else treeFocus = arg;
      render();
    }
    else if (act === "buy-skill") {
      const res = E.buySkill(arg);
      toast(res.msg);
      E.refreshCombat();
      render();
    }
    else if (act === "town") show("town");
    else if (act === "world") show("world");
    else if (act === "stats") show("stats");
    else if (act === "class") show("class");
    else if (act === "combat") show("combat");
    else if (act === "launch") launch();
    else if (act === "pick-class") {
      if (!E.state.classId) E.chooseClass(arg);
      else E.changeClass(arg);
      toast(E.CLASSES[arg].name + " — путь выбран");
      show("hub");
    } else if (act === "town-tab") { townTab = arg; render(); }
    else if (act === "buy") {
      const res = E.buy(arg);
      toast(res.msg);
      render();
    } else if (act === "meta") {
      const res = E.upgradeMeta(arg);
      toast(res.msg);
      render();
    } else if (act === "hire") {
      const res = E.hireMerc(arg);
      toast(res.msg);
      render();
    } else if (act === "dismiss") {
      const res = E.dismissMerc();
      if (res === "full") toast("Рюкзак полон: вещи спутника некуда вернуть");
      else {
        wearWho = "kai";
        toast("Спутник уходит с площади");
      }
      E.refreshCombat();
      render();
    } else if (act === "wear") {
      wearWho = arg === "merc" ? "merc" : "kai";
      render();
    } else if (act === "equip") {
      E.equipItem(Number(arg), wearTarget());
      E.refreshCombat();
      render();
    } else if (act === "unequip") {
      const res = E.unequip(arg, wearTarget());
      if (res === "full") toast("Рюкзак полон");
      E.refreshCombat();
      render();
    } else if (act === "sell") {
      E.sellIndex(Number(arg));
      render();
    } else if (act === "sell-all") {
      if (!E.state.inventory.length) return toast("Продавать нечего");
      if (!confirm("Продать весь рюкзак?")) return;
      const res = E.sellAll();
      toast("Продано " + res.count + " · +" + res.gold);
      render();
    } else if (act === "sell-common") {
      const res = E.sellRarity("common");
      toast(res.count ? "Обычное продано · +" + res.gold : "Обычных вещей нет");
      render();
    } else if (act === "autosell") { E.toggleAutoSell(arg); render(); }
    else if (act === "equip-best") {
      const n = E.equipBest(wearTarget());
      E.refreshCombat();
      toast(n ? "Надето вещей: " + n : "Лучше уже надето");
      render();
    } else if (act === "wipe") {
      if (!confirm("Стереть Кая, башню и кристаллы душ в этом браузере?")) return;
      stopAuto();
      E.reset();
      show("title");
    } else if (act === "fight") {
      E.heroAct(arg);
      render();
    } else if (act === "target") {
      if (E.run) E.run.selected = Number(arg);
      render();
    } else if (act === "next-floor") {
      E.nextFloor();
      render();
    } else if (act === "retreat") {
      if (!confirm("Уйти из башни? Добыча останется, кристаллы душ — нет.")) return;
      stopAuto();
      E.retreat();
      render();
    } else if (act === "end-run") {
      stopAuto();
      E.leaveRun();
      show("hub");
    } else if (act === "toggle-auto") {
      if (!E.run) return;
      E.run.auto = !E.run.auto;
      if (E.run.auto) startAuto();
      else stopAuto();
      render();
    }
  }

  function startAuto() {
    clearInterval(autoTimer);
    autoTimer = setInterval(() => {
      if (!E.run || !E.run.auto || E.run.finished) {
        clearInterval(autoTimer);
        autoTimer = null;
        if (screen === "combat") render();
        return;
      }
      if (screen !== "combat") return;
      if (E.run.between) {
        E.nextFloor();
      } else {
        const hero = E.run.hero;
        if (hero && hero.hp > 0 && hero.hp / hero.maxHp < 0.4 && E.state.potions > 0 && E.run.potionsUsed < 10) {
          E.heroAct("potion");
        }
        if (E.run && !E.run.between && !E.run.finished) E.heroAct(E.pickAuto());
      }
      if (screen === "combat") render();
    }, 900);
  }

  function pauseTimer() {
    clearInterval(autoTimer);
    autoTimer = null;
  }

  function stopAuto() {
    pauseTimer();
    if (E.run) E.run.auto = false;
  }

  document.addEventListener("click", onClick);
  document.addEventListener("keydown", (event) => {
    if (screen !== "combat" || !E.run || E.run.between || E.run.finished) return;
    const key = event.key.toLowerCase();
    if (key === "4" || key === "b") E.heroAct("potion");
    else return;
    event.preventDefault();
    render();
  });

  E.load();
  render();
})(ETERNUM);
