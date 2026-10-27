(function () {
  "use strict";

  /* ============================================================
     Navigasi layar
     ============================================================ */

  var labels = {
    harian: "Harian",
    bulanan: "Bulanan",
    pengaturan: "Pengaturan",
    cari: "Cari Kota",
    onboarding: "Onboarding",
    widget: "Widget",
  };

  var current = "harian";
  var returnTo = "harian";
  var device = document.getElementById("device");
  var caption = document.getElementById("caption-screen");
  var screenEls = Array.prototype.slice.call(document.querySelectorAll(".screen"));
  var railButtons = Array.prototype.slice.call(document.querySelectorAll(".rail button[data-go]"));

  function goto(name) {
    if (!document.getElementById("screen-" + name)) return;

    if (name === "cari") {
      if (current !== "cari") returnTo = current;
      var back = document.querySelector("#screen-cari .page-head button[data-go]");
      if (back) back.dataset.go = returnTo;
    }

    current = name;
    screenEls.forEach(function (screen) {
      screen.classList.toggle("is-active", screen.id === "screen-" + name);
    });
    device.dataset.screen = name;
    caption.textContent = labels[name] || name;

    railButtons.forEach(function (btn) {
      if (btn.dataset.go === name) btn.setAttribute("aria-current", "true");
      else btn.removeAttribute("aria-current");
    });

    Array.prototype.forEach.call(document.querySelectorAll(".bottom-bar button"), function (btn) {
      var inThisScreen = btn.closest(".screen").id === "screen-" + name;
      if (inThisScreen && btn.dataset.go === name) btn.setAttribute("aria-current", "true");
      else btn.removeAttribute("aria-current");
    });

    Array.prototype.forEach.call(document.querySelectorAll(".notes-section"), function (section) {
      section.hidden = section.dataset.notes !== name;
    });

    if (name === "onboarding") setObStep(0);

    var scroller = document.querySelector("#screen-" + name + " .screen-scroll");
    if (scroller) scroller.scrollTop = 0;
  }

  Array.prototype.forEach.call(document.querySelectorAll("[data-go]"), function (btn) {
    btn.addEventListener("click", function () {
      var target = btn.dataset.go;
      if (target) goto(target);
    });
  });

  /* ============================================================
     Tema: terang / gelap / ikuti sistem
     ============================================================ */

  var themeButtons = Array.prototype.slice.call(document.querySelectorAll("[data-theme-choice]"));
  var appThemeButtons = Array.prototype.slice.call(document.querySelectorAll("[data-app-theme]"));

  function setTheme(mode) {
    if (mode === "system") document.documentElement.removeAttribute("data-theme");
    else document.documentElement.setAttribute("data-theme", mode);

    themeButtons.forEach(function (btn) {
      btn.setAttribute("aria-pressed", String(btn.dataset.themeChoice === mode));
    });
    appThemeButtons.forEach(function (btn) {
      btn.setAttribute("aria-pressed", String(btn.dataset.appTheme === mode));
    });
  }

  themeButtons.forEach(function (btn) {
    btn.addEventListener("click", function () {
      setTheme(btn.dataset.themeChoice);
    });
  });

  appThemeButtons.forEach(function (btn) {
    btn.addEventListener("click", function () {
      setTheme(btn.dataset.appTheme);
    });
  });

  /* ============================================================
     Hitung mundur
     ============================================================ */

  var remaining = 2 * 60 * 60 + 28 * 60 + 14;
  var cd = {
    h: document.querySelector('[data-cd="h"]'),
    m: document.querySelector('[data-cd="m"]'),
    s: document.querySelector('[data-cd="s"]'),
  };
  var countdownEl = document.getElementById("countdown");

  function pad(value) {
    return String(value).padStart(2, "0");
  }

  function renderCountdown() {
    var h = Math.floor(remaining / 3600);
    var m = Math.floor((remaining % 3600) / 60);
    var s = remaining % 60;

    cd.h.textContent = pad(h);
    cd.m.textContent = pad(m);
    cd.s.textContent = pad(s);

    if (s === 0) {
      countdownEl.setAttribute("aria-label", "Dzuhur dalam " + h + " jam " + m + " menit");
    }
  }

  renderCountdown();
  setInterval(function () {
    if (remaining > 0) {
      remaining -= 1;
      renderCountdown();
    }
  }, 1000);

  /* ============================================================
     Ikon lonceng per waktu
     ============================================================ */

  Array.prototype.forEach.call(document.querySelectorAll(".prayer-row .bell"), function (bell) {
    bell.addEventListener("click", function () {
      var on = bell.dataset.on !== "true";
      var name = bell.closest(".prayer-row").querySelector(".p-name").textContent;
      bell.dataset.on = String(on);
      bell.setAttribute("aria-pressed", String(on));
      bell.setAttribute(
        "aria-label",
        "Notifikasi " + name + " " + (on ? "aktif" : "nonaktif")
      );
    });
  });

  /* ============================================================
     Banner izin + toggle adzan
     ============================================================ */

  var banner = document.getElementById("perm-banner");
  var swAdzan = document.getElementById("sw-adzan");

  document.getElementById("enable-notif").addEventListener("click", function () {
    banner.hidden = true;
    swAdzan.setAttribute("aria-checked", "true");
  });

  /* ============================================================
     Toggle umum
     ============================================================ */

  Array.prototype.forEach.call(document.querySelectorAll(".switch"), function (sw) {
    sw.addEventListener("click", function () {
      var on = sw.getAttribute("aria-checked") !== "true";
      sw.setAttribute("aria-checked", String(on));
      if (sw === swAdzan) banner.hidden = on;
    });
  });

  /* ============================================================
     Ihtiyath
     ============================================================ */

  var ihtiyath = 1;
  var ihtiyathOut = document.getElementById("ihtiyath-value");

  function renderIhtiyath() {
    ihtiyathOut.textContent = ihtiyath > 0 ? "+" + ihtiyath : String(ihtiyath);
  }

  Array.prototype.forEach.call(document.querySelectorAll("[data-ihtiyath]"), function (btn) {
    btn.addEventListener("click", function () {
      ihtiyath = Math.max(0, Math.min(5, ihtiyath + Number(btn.dataset.ihtiyath)));
      renderIhtiyath();
    });
  });

  /* ============================================================
     Jadwal bulanan
     ============================================================ */

  var monthBody = document.getElementById("month-body");
  var baseTimes = [4 * 60 + 28, 11 * 60 + 52, 15 * 60 + 12, 17 * 60 + 52, 19 * 60 + 2];

  function formatTime(totalMinutes) {
    var minutes = ((totalMinutes % 1440) + 1440) % 1440;
    return pad(Math.floor(minutes / 60)) + "." + pad(minutes % 60);
  }

  function renderMonth() {
    var rows = "";
    for (var day = 1; day <= 31; day += 1) {
      var wobble = Math.round(Math.sin((day + 2) / 3.1));
      var times = baseTimes.map(function (base, index) {
        if (index === 0) return formatTime(base - wobble);
        if (index === 1) return formatTime(base + Math.round(wobble / 2));
        return formatTime(base + wobble);
      });
      rows +=
        '<tr' +
        (day === 27 ? ' class="is-today" aria-current="date"' : "") +
        "><td>" +
        day +
        "</td>" +
        times
          .map(function (time) {
            return "<td>" + time + "</td>";
          })
          .join("") +
        "</tr>";
    }
    monthBody.innerHTML = rows;
  }

  /* ============================================================
     Cari kota
     ============================================================ */

  var cities = [
    { name: "Jakarta Selatan", province: "DKI Jakarta" },
    { name: "Jakarta Pusat", province: "DKI Jakarta" },
    { name: "Bandung", province: "Jawa Barat" },
    { name: "Bandung Barat", province: "Jawa Barat" },
    { name: "Bekasi", province: "Jawa Barat" },
    { name: "Semarang", province: "Jawa Tengah" },
    { name: "Surabaya", province: "Jawa Timur" },
    { name: "Malang", province: "Jawa Timur" },
    { name: "Yogyakarta", province: "DI Yogyakarta" },
    { name: "Medan", province: "Sumatera Utara" },
    { name: "Palembang", province: "Sumatera Selatan" },
    { name: "Makassar", province: "Sulawesi Selatan" },
    { name: "Denpasar", province: "Bali" },
    { name: "Banjarmasin", province: "Kalimantan Selatan" },
  ];

  var selectedCity = "Jakarta Selatan";
  var cityInput = document.getElementById("city-search");
  var cityResults = document.getElementById("city-results");
  var searchHint = document.getElementById("search-hint");

  var pinIcon =
    '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M12 21s7-5.4 7-11a7 7 0 1 0-14 0c0 5.6 7 11 7 11Z"/><circle cx="12" cy="10" r="2.4"/></svg>';

  var checkIcon =
    '<svg class="check" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m5 13 4 4L19 7"/></svg>';

  function syncLocation() {
    document.getElementById("loc-name").textContent = selectedCity;
    document.getElementById("loc-foot").textContent = selectedCity;
    document.getElementById("loc-name-settings").textContent = selectedCity;
    document.getElementById("ob-loc").textContent = selectedCity;
    document.getElementById("widget-loc").textContent = selectedCity + " · 27 Okt";
  }

  function renderCities(query) {
    var q = (query || "").trim().toLowerCase();
    var filtered = cities.filter(function (city) {
      return (
        !q ||
        city.name.toLowerCase().indexOf(q) !== -1 ||
        city.province.toLowerCase().indexOf(q) !== -1
      );
    });

    if (!filtered.length) {
      cityResults.innerHTML =
        '<div class="empty-state"><strong>Kota tidak ditemukan</strong><span>Coba kata kunci lain, misalnya “bandung” atau “jawa”.</span></div>';
      searchHint.textContent = "0 kota · pencarian offline";
      return;
    }

    searchHint.textContent =
      (q ? filtered.length + " hasil" : "Kota populer") + " · pencarian berjalan offline";

    cityResults.innerHTML = filtered
      .map(function (city) {
        var isSelected = city.name === selectedCity;
        return (
          '<button type="button" class="result-row" aria-label="Pilih ' +
          city.name +
          ", " +
          city.province +
          '"' +
          (isSelected ? ' aria-current="true"' : "") +
          ' data-city="' +
          city.name +
          '">' +
          '<span class="pin" aria-hidden="true">' +
          pinIcon +
          "</span>" +
          '<span class="r-main"><span class="r-city">' +
          city.name +
          '</span><span class="r-prov">' +
          city.province +
          "</span></span>" +
          (isSelected ? checkIcon : "") +
          "</button>"
        );
      })
      .join("");

    Array.prototype.forEach.call(cityResults.querySelectorAll(".result-row"), function (row) {
      row.addEventListener("click", function () {
        selectedCity = row.dataset.city;
        cityInput.value = "";
        syncLocation();
        renderCities("");
        goto("harian");
      });
    });
  }

  cityInput.addEventListener("input", function () {
    renderCities(cityInput.value);
  });

  /* ============================================================
     Onboarding
     ============================================================ */

  var obSteps = Array.prototype.slice.call(document.querySelectorAll(".ob-step"));
  var obIndex = 0;

  function setObStep(index) {
    obIndex = Math.max(0, Math.min(obSteps.length - 1, index));
    obSteps.forEach(function (step, i) {
      step.classList.toggle("is-active", i === obIndex);
    });
  }

  Array.prototype.forEach.call(
    document.querySelectorAll("[data-ob-next], [data-ob-skip]"),
    function (btn) {
      btn.addEventListener("click", function () {
        if (obIndex < obSteps.length - 1) setObStep(obIndex + 1);
        else goto("harian");
      });
    }
  );

  document.querySelector("[data-ob-finish]").addEventListener("click", function () {
    goto("harian");
  });

  /* ============================================================
     Inisialisasi
     ============================================================ */

  var params = new URLSearchParams(window.location.search);
  var initialTheme =
    params.get("theme") || document.documentElement.getAttribute("data-theme") || "system";
  if (["light", "dark", "system"].indexOf(initialTheme) === -1) initialTheme = "light";
  setTheme(initialTheme);
  renderMonth();
  renderCities("");
  renderIhtiyath();
  syncLocation();

  var initialScreen = params.get("screen");
  goto(labels[initialScreen] ? initialScreen : "harian");

  var stepParam = parseInt(params.get("step"), 10);
  if (initialScreen === "onboarding" && stepParam >= 1 && stepParam <= obSteps.length) {
    setObStep(stepParam - 1);
  }
})();
