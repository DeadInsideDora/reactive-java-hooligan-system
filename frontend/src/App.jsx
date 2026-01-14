import React from "react";
import {
  HashRouter,
  Routes,
  Route,
  Link,
  NavLink,
  useNavigate,
  useParams,
  Navigate,
  useLocation,
} from "react-router-dom";

const API_BASE = import.meta.env.VITE_API_BASE || "";
const AUTH_KEY = "hg-auth";
const SLOT_KEY = "hg-slot";
const ACH_KEY = "hg-achievements";

const authStorage = {
  get() {
    const raw = localStorage.getItem(AUTH_KEY);
    return raw ? JSON.parse(raw) : null;
  },
  set(value) {
    localStorage.setItem(AUTH_KEY, JSON.stringify(value));
  },
  clear() {
    localStorage.removeItem(AUTH_KEY);
  },
};

const achievementsStorage = {
  get(username) {
    const raw = localStorage.getItem(ACH_KEY);
    const data = raw ? JSON.parse(raw) : {};
    return data[username] || {
      likes: 0,
      dislikes: 0,
      comments: 0,
      incidents: 0,
      alerts: 0,
      moderations: 0,
    };
  },
  bump(username, key) {
    const raw = localStorage.getItem(ACH_KEY);
    const data = raw ? JSON.parse(raw) : {};
    const current = data[username] || {
      likes: 0,
      dislikes: 0,
      comments: 0,
      incidents: 0,
      alerts: 0,
      moderations: 0,
    };
    data[username] = { ...current, [key]: current[key] + 1 };
    localStorage.setItem(ACH_KEY, JSON.stringify(data));
    return data[username];
  },
};

const reportsApi = {
  list() {
    return apiFetch("/api/reports");
  },
  create(incidentId, commentId, reason) {
    return apiFetch("/api/reports", {
      method: "POST",
      body: JSON.stringify({ incidentId, commentId, reason }),
    });
  },
  remove(id) {
    return apiFetch(`/api/reports/${id}`, { method: "DELETE" });
  },
};

function getUserRole(auth) {
  if (!auth?.role) return "STUDENT";
  return auth.role;
}

const slotStorage = {
  get() {
    const raw = localStorage.getItem(SLOT_KEY);
    return raw ? JSON.parse(raw) : null;
  },
  set(value) {
    localStorage.setItem(SLOT_KEY, JSON.stringify(value));
  },
  clear() {
    localStorage.removeItem(SLOT_KEY);
  },
};

function buildAuthHeader(auth) {
  if (!auth?.username || !auth?.password) return {};
  const token = btoa(`${auth.username}:${auth.password}`);
  return { Authorization: `Basic ${token}` };
}

async function apiFetch(path, options = {}) {
  const auth = authStorage.get();
  return apiFetchWithAuth(path, auth, options);
}

async function apiFetchWithAuth(path, auth, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
    ...buildAuthHeader(auth),
  };

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 204) return null;
  const text = await response.text();
  if (!response.ok) {
    let message = text || response.statusText;
    try {
      const parsed = JSON.parse(text);
      if (parsed?.message) {
        message = parsed.message;
      }
    } catch (err) {
      /* ignore json parse error */
    }
    throw new Error(message);
  }
  return text ? JSON.parse(text) : null;
}

function buildStreamUrl(path = "/api/incidents/stream") {
  const auth = authStorage.get();
  const url = new URL(path, window.location.origin);
  if (auth?.username && auth?.password) {
    url.username = auth.username;
    url.password = auth.password;
  }
  return url.toString();
}

function useFetchState(initial) {
  const [data, setData] = React.useState(initial);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState("");

  const run = React.useCallback(async (fn) => {
    setLoading(true);
    setError("");
    try {
      const result = await fn();
      setData(result);
      return result;
    } catch (err) {
      setError(err.message || "Ошибка запроса");
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  return { data, setData, loading, error, run };
}

function PageShell({ title, subtitle, children }) {
  return (
    <section className="relative z-10 px-6 pb-16 pt-10">
      <div className="mx-auto w-full max-w-6xl">
        <div className="mb-8 reveal">
          <p className="text-sm uppercase tracking-[0.3em] text-mint/70">
            HooliGuns System
          </p>
          <h1 className="font-display text-4xl text-neon md:text-5xl">{title}</h1>
          {subtitle ? (
            <p className="mt-2 max-w-2xl text-slate-300">{subtitle}</p>
          ) : null}
        </div>
        {children}
      </div>
    </section>
  );
}

function Layout({ children }) {
  const auth = authStorage.get();
  const role = getUserRole(auth);
  const navigate = useNavigate();
  const [alerts, setAlerts] = React.useState([]);
  const location = useLocation();
  const isLogin = location.pathname === "/login";

  const logout = () => {
    authStorage.clear();
    navigate("/login");
  };

  React.useEffect(() => {
    apiFetch("/api/alerts")
      .then((list) => setAlerts(list || []))
      .catch(() => setAlerts([]));
    let pollTimer;
    const onLocalAlert = (event) => {
      const payload = event.detail;
      if (payload) {
        setAlerts((prev) => [payload, ...prev].slice(0, 50));
      }
    };
    window.addEventListener("alert:new", onLocalAlert);
    const source = new EventSource(buildStreamUrl("/api/alerts/stream"));
    source.onmessage = (event) => {
      const payload = JSON.parse(event.data);
      setAlerts((prev) => [payload, ...prev].slice(0, 50));
    };
    source.onerror = () => {
      source.close();
      pollTimer = setInterval(() => {
        apiFetch("/api/alerts")
          .then((list) => setAlerts(list || []))
          .catch(() => {});
      }, 6000);
    };
    return () => {
      window.removeEventListener("alert:new", onLocalAlert);
      source.close();
      if (pollTimer) clearInterval(pollTimer);
    };
  }, []);

  return (
    <div className="min-h-screen font-body text-slate-100 bg-night flex flex-col">
      <div className="pointer-events-none absolute inset-0 grid-haze opacity-60" />
      <div className="pointer-events-none absolute right-0 top-0 h-64 w-64 glow-ring" />
      <header className="relative z-20 border-b border-white/5 bg-dusk/70 backdrop-blur">
        <div className="mx-auto flex w-full max-w-6xl items-center justify-between px-6 py-4">
          <Link to="/" className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-ember/30 text-xl font-display text-neon shadow-glow">
              777
            </div>
            <div>
              <p className="font-display text-2xl leading-none text-white">HooliGuns</p>
              <p className="text-xs uppercase tracking-[0.3em] text-slate-400">
                reactive campus
              </p>
            </div>
          </Link>
          <nav className="hidden items-center gap-4 text-sm text-slate-300 md:flex">
            <NavLink to="/incidents" className="nav-link">
              Нарушения
            </NavLink>
            <NavLink to="/stats" className="nav-link">
              Статистика
            </NavLink>
            <NavLink to="/achievements" className="nav-link">
              Ачивки
            </NavLink>
            {role === "ADMIN" || role === "TEACHER" || role === "IMMORTAL" ? (
              <>
                <NavLink to="/slot" className="nav-link">
                  Слот-машина
                </NavLink>
                <NavLink to="/admin" className="nav-link">
                  Админ
                </NavLink>
              </>
            ) : null}
            {role === "IMMORTAL" ? (
              <NavLink to="/immortal" className="nav-link">
                Бессмертный
              </NavLink>
            ) : null}
            {auth ? (
              <button
                className="rounded-full border border-white/10 px-4 py-2 text-xs uppercase tracking-[0.3em] text-white hover:border-white/30"
                onClick={logout}
              >
                Выйти
              </button>
            ) : (
              <NavLink to="/login" className="nav-link">
                Войти
              </NavLink>
            )}
          </nav>
          <div className="md:hidden">
            <NavLink to="/incidents" className="nav-link">
              Меню
            </NavLink>
          </div>
        </div>
      </header>
      <main className="relative z-10 mx-auto w-full max-w-7xl px-6 py-4 flex-1">
        <div
          className={
            "grid gap-6" +
            (isLogin ? "" : " lg:grid-cols-[1fr_320px] lg:items-start")
          }
        >
          <div>{children}</div>
          {isLogin ? null : <AlertsColumn alerts={alerts} />}
        </div>
      </main>
      <footer className="relative z-10 border-t border-white/5 bg-dusk/60 px-6 py-6 text-xs text-slate-400 mt-6">
        <div className="mx-auto flex w-full max-w-6xl flex-col gap-2 md:flex-row md:items-center md:justify-between">
          <span>Voronina, Konovalov</span>
          <span>ITMO, 2026</span>
        </div>
      </footer>
    </div>
  );
}

function AlertsColumn({ alerts }) {
  return (
    <aside className="card-glass hidden h-full max-h-[calc(100vh-7rem)] flex-col rounded-3xl border border-ember/30 p-4 lg:mt-10 lg:flex">
      <div className="flex items-center justify-between text-xs uppercase tracking-[0.3em] text-neon">
        <span>Alerts</span>
        <span className="text-slate-400">{alerts.length}</span>
      </div>
      <div className="mt-3 flex-1 space-y-3 overflow-y-auto pr-1">
        {alerts.length === 0 ? (
          <p className="text-xs text-slate-400">Нет алертов</p>
        ) : (
          alerts.map((alert) => (
            <div
              key={alert.id}
              className="rounded-2xl border border-ember/40 bg-black/30 px-4 py-3 text-xs text-slate-200 shadow"
            >
              <p className="text-neon">ALERT</p>
              <p className="mt-1">{alert.message}</p>
              <p className="mt-2 text-[10px] text-slate-400">
                {new Date(alert.createdAt).toLocaleString()}
              </p>
            </div>
          ))
        )}
      </div>
    </aside>
  );
}

function LoginPage() {
  const navigate = useNavigate();
  const [form, setForm] = React.useState({
    username: "",
    password: "",
  });
  const [createForm, setCreateForm] = React.useState({
    username: "",
    password: "",
    displayName: "",
    faculty: "",
    groupName: "",
  });
  const { loading, error, run } = useFetchState(null);
  const [createMessage, setCreateMessage] = React.useState("");

  const submit = async (event) => {
    event.preventDefault();
    await run(async () => {
      const normalized = form.username.trim().toLowerCase();
      const user = await apiFetchWithAuth("/api/users/me", {
        username: normalized,
        password: form.password,
      });
      authStorage.set({ username: normalized, password: form.password, role: user.role });
      navigate("/incidents");
    });
  };

  const submitCreate = async (event) => {
    event.preventDefault();
    setCreateMessage("");
    try {
      await apiFetch("/api/users/register", {
        method: "POST",
        body: JSON.stringify({
          username: createForm.username,
          password: createForm.password,
          displayName: createForm.displayName,
          faculty: createForm.faculty || null,
          groupName: createForm.groupName || null,
        }),
      });
      setCreateMessage("Пользователь создан.");
      setCreateForm({
        username: "",
        password: "",
        displayName: "",
        faculty: "",
        groupName: "",
      });
    } catch (err) {
      setCreateMessage(err.message || "Ошибка создания пользователя");
    }
  };

  return (
    <PageShell
      title="Авторизация"
    >
      <div className="grid gap-6 lg:grid-cols-[1.2fr_1fr]">
        <form
          onSubmit={submit}
          className="card-glass rounded-3xl p-8 text-slate-200 shadow-glow"
        >
          <h3 className="mb-6 font-display text-2xl text-neon">Войти в аккаунт</h3>
          <div className="mb-6">
            <label className="text-xs uppercase tracking-[0.3em] text-slate-400">
              ISU
            </label>
            <input
              className="mt-2 w-full rounded-2xl border border-white/10 bg-black/30 px-4 py-3 text-white"
              placeholder="sXXXXXX"
              value={form.username}
              onChange={(event) =>
                setForm((prev) => ({ ...prev, username: event.target.value }))
              }
              required
            />
          </div>
          <div className="mb-6">
            <label className="text-xs uppercase tracking-[0.3em] text-slate-400">
              Пароль
            </label>
            <input
              type="password"
              className="mt-2 w-full rounded-2xl border border-white/10 bg-black/30 px-4 py-3 text-white"
              value={form.password}
              onChange={(event) =>
                setForm((prev) => ({ ...prev, password: event.target.value }))
              }
              required
            />
          </div>
          {error ? <p className="mb-4 text-sm text-ember">{error}</p> : null}
          <button
            className="w-full rounded-full bg-ember px-6 py-3 text-sm font-semibold uppercase tracking-[0.3em] text-black"
            disabled={loading}
          >
            {loading ? "Проверка..." : "Войти"}
          </button>
        </form>
        <div className="space-y-6">
          <form onSubmit={submitCreate} className="card-glass rounded-3xl p-8">
            <h3 className="font-display text-2xl text-neon">Зарегистрироваться</h3>
            <div className="mt-4 grid gap-3">
              <input
                placeholder="ISU (sXXXXXX)"
                className="rounded-2xl border border-white/10 bg-black/30 px-4 py-2 text-white"
                value={createForm.username}
                onChange={(event) =>
                  setCreateForm((prev) => ({ ...prev, username: event.target.value }))
                }
                required
              />
              <input
                type="password"
                placeholder="Пароль"
                className="rounded-2xl border border-white/10 bg-black/30 px-4 py-2 text-white"
                value={createForm.password}
                onChange={(event) =>
                  setCreateForm((prev) => ({ ...prev, password: event.target.value }))
                }
                required
              />
              <input
                placeholder="Отображаемое имя"
                className="rounded-2xl border border-white/10 bg-black/30 px-4 py-2 text-white"
                value={createForm.displayName}
                onChange={(event) =>
                  setCreateForm((prev) => ({
                    ...prev,
                    displayName: event.target.value,
                  }))
                }
                required
              />
            <select
              className="rounded-2xl border border-white/10 bg-black/30 px-4 py-2 text-white"
              value={createForm.faculty}
              onChange={(event) =>
                setCreateForm((prev) => ({ ...prev, faculty: event.target.value }))
              }
            >
              <option value="">Факультет</option>
              <option value="ФСУиР">ФСУиР</option>
              <option value="ФПИиКТ">ФПИиКТ</option>
              <option value="ФБИТ">ФБИТ</option>
              <option value="ФИТиП">ФИТиП</option>
              <option value="ФПИ">ФПИ</option>
              <option value="ФТИИ">ФТИИ</option>
              <option value="ИНФОХИМИЯ">ИНФОХИМИЯ</option>
              <option value="ФЭкотехнологий">ФЭкотехнологий</option>
              <option value="ФБиотехнологий">ФБиотехнологий</option>
              <option value="ФТМиИ">ФТМиИ</option>
            </select>
              <input
                placeholder="Группа"
                className="rounded-2xl border border-white/10 bg-black/30 px-4 py-2 text-white"
                value={createForm.groupName}
                onChange={(event) =>
                  setCreateForm((prev) => ({
                    ...prev,
                    groupName: event.target.value,
                  }))
                }
              />
              <button className="rounded-full bg-mint px-6 py-3 text-xs uppercase tracking-[0.3em] text-black">
                Создать пользователя
              </button>
              {createMessage ? (
                <p className="text-xs text-slate-300">{createMessage}</p>
              ) : null}
            </div>
          </form>
        </div>
      </div>
    </PageShell>
  );
}

function IncidentsPage() {
  const auth = authStorage.get();
  const role = getUserRole(auth);
  const username = auth?.username || "anon";
  const [filters, setFilters] = React.useState({
    from: "",
    type: "",
    q: "",
  });
  const [comments, setComments] = React.useState({});
  const [board, setBoard] = React.useState([]);
  const { data, setData, loading, error, run } = useFetchState([]);
  const [streamStatus, setStreamStatus] = React.useState("connecting");

  const fetchIncidents = React.useCallback(async () => {
    const params = new URLSearchParams();
    if (filters.q) params.set("q", filters.q);
    if (filters.from) params.set("from", new Date(filters.from).toISOString());
    if (filters.to) params.set("to", new Date(filters.to).toISOString());
    if (filters.type) params.set("type", filters.type);
    return apiFetch(`/api/incidents?${params.toString()}`);
  }, [filters]);

  React.useEffect(() => {
    run(fetchIncidents);
  }, [fetchIncidents, run]);

  React.useEffect(() => {
    let pollTimer;
    const source = new EventSource(buildStreamUrl());
    source.onmessage = (event) => {
      setStreamStatus("live");
      const payload = JSON.parse(event.data);
      setData((prev) => {
        const existing = prev.find((item) => item.id === payload.id);
        if (existing) {
          return prev.map((item) => (item.id === payload.id ? payload : item));
        }
        return [payload, ...prev];
      });
    };
    source.onerror = () => {
      setStreamStatus("polling");
      source.close();
      pollTimer = setInterval(() => {
        fetchIncidents().then((result) => {
          if (result) setData(result);
        });
      }, 6000);
    };
    return () => {
      source.close();
      if (pollTimer) clearInterval(pollTimer);
    };
  }, [fetchIncidents, setData]);

  React.useEffect(() => {
    apiFetch("/api/hooligans/board?limit=3")
      .then(setBoard)
      .catch(() => setBoard([]));
  }, []);

  const sendReaction = async (id, type) => {
    await apiFetch(`/api/incidents/${id}/reactions`, {
      method: "POST",
      body: JSON.stringify({ type }),
    });
    achievementsStorage.bump(username, type === "LIKE" ? "likes" : "dislikes");
    run(fetchIncidents);
  };

  const sendComment = async (id, text, reset) => {
    await apiFetch(`/api/incidents/${id}/comments`, {
      method: "POST",
      body: JSON.stringify({ text }),
    });
    achievementsStorage.bump(username, "comments");
    reset();
    const list = await apiFetch(`/api/incidents/${id}/comments`);
    setComments((prev) => ({ ...prev, [id]: list || [] }));
  };

  const loadComments = async (id) => {
    const list = await apiFetch(`/api/incidents/${id}/comments`);
    setComments((prev) => ({ ...prev, [id]: list || [] }));
  };

  return (
    <PageShell
      title="Нарушения"
    >
      <div className="mb-6 grid gap-4 md:grid-cols-[2fr_1fr]">
        <div className="card-glass rounded-3xl p-6">
          <div className="flex flex-wrap items-center gap-3">
            <input
              placeholder="Поиск по тексту"
              className="flex-1 rounded-full border border-white/10 bg-black/30 px-4 py-2 text-sm text-white"
              value={filters.q}
              onChange={(event) =>
                setFilters((prev) => ({ ...prev, q: event.target.value }))
              }
            />
            <select
              className="rounded-full border border-white/10 bg-black/30 px-4 py-2 text-sm text-white"
              value={filters.type}
              onChange={(event) =>
                setFilters((prev) => ({ ...prev, type: event.target.value }))
              }
            >
              <option value="">Все типы</option>
              <option value="DISRUPTION">DISRUPTION</option>
              <option value="DAMAGE">DAMAGE</option>
              <option value="CHEATING">CHEATING</option>
              <option value="AGGRESSION">AGGRESSION</option>
              <option value="OTHER">OTHER</option>
            </select>
            <input
              type="datetime-local"
              className="rounded-full border border-white/10 bg-black/30 px-4 py-2 text-xs text-white"
              value={filters.from}
              onChange={(event) =>
                setFilters((prev) => ({ ...prev, from: event.target.value }))
              }
            />
          </div>
          <div className="mt-6 flex flex-col items-center space-y-4 text-xs text-slate-400">
            <pre className="whitespace-pre-wrap font-mono leading-5 text-center">
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⠀⢀⣀⠀⠀⠀{`\n`}
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠜⠁⣷⢊⠤⡇⠀⠀{`\n`}
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣀⡠⠴⠨⠼⡜⡝⠀⠀⠀{`\n`}
⠀⢀⣀⣀⣠⡤⠔⠒⠚⠉⢙⣖⡎⢰⣞⣶⠄⠘⢣⠀⠀⠀{`\n`}
⢰⣿⣿⣿⣿⣿⠀⠀⠀⠰⠀⠎⠁⠈⠾⠏⠀⠀⠈⡆⠀⠀{`\n`}
⢸⣿⣿⣿⣿⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠀{`\n`}
⢸⣿⣿⣿⣿⣿⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⢄⡀⠂⡇⠀⠀{`\n`}
⠀⠙⠿⣿⡿⠟⠀⠀⠀⠀⠀⠀⠀⣀⣠⠮⠀⠠⢈⠃⠀⠀{`\n`}
⠀⠀⠀⠀⠉⠒⠒⠒⡆⠀⠀⠈⡽⠥⠄⠀⠄⢀⣼⡀⠀⠀{`\n`}
⠀⠀⠀⠀⠀⠀⠀⢀⠏⠓⠶⠖⣒⣲⡲⠮⠝⠛⠁⠈⢆⠀{`\n`}
⠀⠀⠀⠀⠀⠀⠀⡌⡆⠀⠀⠀⠀⠀⠀⠀⠀⠸⠁⠀⠈⡆{`\n`}
⠀⠀⠀⠀⠀⠀⠀⢏⢇⠀⠀⠀⠀⠀⠀⠀⠀⣠⠓⠋⠀⣀{`\n`}
⠀⠀⠀⠀⠀⠀⠀⠈⢺⠓⢶⢒⣶⣲⣶⡒⠶⠲⣄⣀⡸⠉{`\n`}
⠀⠀⠀⠀⠀⠀⠀⠀⠀⡇⠀⠃⠻⣥⠥⠄⠀⠐⠔⢸⠇⠀{`\n`}
⠀⠀⠀⠀⠀⠀⠀⠀⠀⢣⢠⣄⠀⢻⡀⢀⠀⠀⠀⢸⡇⠀{`\n`}
⠀⠀⠀⠀⠀⠀⠀⠀⠀⢘⡀⠙⠀⠘⡃⠘⠄⠀⠀⢸⡇⠀{`\n`}
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠨⣌⠉⠉⠉⠹⠉⠒⠒⠒⠊⣱⠀{`\n`}
⠀⠀⠀⠀⠀⠀⠀⠀⡔⠉⠉⠻⣫⢗⣣⡶⢶⣷⣶⠞⡏⠀{`\n`}
⠀⠀⠀⠀⠀⠀⠀⠀⠱⣤⣤⣤⡽⡮⣏⠀⠀⠈⣛⢿⠇⠀{`\n`}
⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠒⠒⠒⠒⠁⠀⠀
            </pre>
            <a
              href="https://neerc.ifmo.ru/lgd.pdf"
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-2 rounded-full border border-white/10 px-5 py-3 text-sm uppercase tracking-[0.3em] text-neon hover:border-neon/60 hover:bg-neon/10"
            >
              Legend?
            </a>
          </div>
        </div>
        <div className="card-glass rounded-3xl p-6">
          <h3 className="font-display text-2xl text-neon">Топ нарушителей</h3>
          <div className="mt-4 space-y-3 text-sm text-slate-300">
            {board.length === 0 ? (
              <p>Нет данных</p>
            ) : (
              board.map((entry) => (
                <Link
                  to={`/hooligan/${entry.userId}`}
                  key={entry.userId}
                  className="flex items-center justify-between rounded-2xl border border-white/5 bg-black/20 px-4 py-3 hover:border-neon/50"
                >
                  <span>{entry.displayName}</span>
                  <span className="text-neon">{entry.points.toFixed(1)}</span>
                </Link>
              ))
            )}
          </div>
        </div>
      </div>
      <div className="space-y-6">
        {data && data.length ? (
          data.map((incident) => (
            <IncidentCard
              key={incident.id}
              incident={incident}
              comments={comments[incident.id]}
              onComment={() => loadComments(incident.id)}
              onReact={sendReaction}
              onSendComment={sendComment}
              role={role}
            />
          ))
        ) : (
          <div className="card-glass rounded-3xl p-8 text-slate-300">
            {loading ? "Загрузка..." : "Нет данных по нарушениям"}
          </div>
        )}
      </div>
    </PageShell>
  );
}

function IncidentCard({
  incident,
  comments,
  onComment,
  onReact,
  onSendComment,
  role,
}) {
  const [commentText, setCommentText] = React.useState("");
  const [expanded, setExpanded] = React.useState(false);

  const submitComment = () => {
    if (!commentText.trim()) return;
    onSendComment(incident.id, commentText, () => setCommentText(""));
  };

  const reportComment = async (comment) => {
    await reportsApi.create(incident.id, comment.id, "Нарушение контента");
  };

  const removeComment = async (comment) => {
    await apiFetch(`/api/incidents/${incident.id}/comments/${comment.id}`, {
      method: "DELETE",
    });
    onComment();
  };

  return (
    <article className="card-glass reveal rounded-3xl p-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-xs uppercase tracking-[0.3em] text-slate-400">
            {incident.type} • {new Date(incident.occurredAt).toLocaleString()}
          </p>
          <h3 className="mt-2 text-2xl font-semibold text-white">
            {incident.title}
          </h3>
          <p className="mt-2 text-sm text-slate-300">{incident.description}</p>
          <p className="mt-2 text-xs text-slate-500">{incident.place}</p>
          <p className="mt-2 text-xs text-slate-400">
            ISU нарушителя: {incident.offenderId}
          </p>
          <p className="mt-2 text-xs text-slate-400">
            Наказание: {incident.punishment || "NONE"}
          </p>
        </div>
        <Link
          to={`/hooligan/${incident.offenderId}`}
          className="rounded-full border border-white/10 px-4 py-2 text-xs uppercase tracking-[0.3em] text-neon hover:border-neon/60"
        >
          Карточка нарушителя
        </Link>
      </div>
      <div className="mt-4 flex flex-wrap items-center gap-3 text-xs text-slate-300">
        <span className="chip rounded-full px-3 py-1">
          Статус: {incident.moderationStatus}
        </span>
        <span>Лайки: {incident.likes}</span>
        <span>Дизлайки: {incident.dislikes}</span>
        <span>Комментарии: {incident.comments}</span>
      </div>
      <div className="mt-4 flex flex-wrap gap-3">
        <button
          className="rounded-full bg-mint/20 px-4 py-2 text-xs uppercase tracking-[0.3em] text-mint hover:bg-mint/30"
          onClick={() => onReact(incident.id, "LIKE")}
        >
          Поддержать
        </button>
        <button
          className="rounded-full bg-ember/20 px-4 py-2 text-xs uppercase tracking-[0.3em] text-ember hover:bg-ember/40"
          onClick={() => onReact(incident.id, "DISLIKE")}
        >
          Осудить
        </button>
        <button
          className="rounded-full border border-white/10 px-4 py-2 text-xs uppercase tracking-[0.3em] text-white hover:border-white/40"
          onClick={() => {
            setExpanded((prev) => !prev);
            if (!comments) onComment();
          }}
        >
          {expanded ? "Скрыть" : "Комментарии"}
        </button>
      </div>
      {expanded ? (
        <div className="mt-4 space-y-3 text-sm text-slate-300">
          <div className="flex gap-3">
            <input
              className="flex-1 rounded-2xl border border-white/10 bg-black/30 px-4 py-2 text-sm text-white"
              placeholder="Оставить комментарий"
              value={commentText}
              onChange={(event) => setCommentText(event.target.value)}
            />
            <button
              className="rounded-full bg-neon px-4 py-2 text-xs uppercase tracking-[0.3em] text-black"
              onClick={submitComment}
            >
              Отправить
            </button>
          </div>
          <div className="space-y-2">
            {(comments || []).length === 0 ? (
              <p>Пока без комментариев</p>
            ) : (
              comments.map((item, index) => (
                <div
                  key={`${incident.id}-comment-${index}`}
                  className="rounded-2xl border border-white/5 bg-black/20 px-4 py-3"
                >
                  <p className="text-xs uppercase tracking-[0.3em] text-slate-500">
                    {item.displayName}
                  </p>
                  <p className="mt-2">{item.text}</p>
                  <p className="mt-1 text-xs text-slate-500">
                    {new Date(item.createdAt).toLocaleString()}
                  </p>
                  <div className="mt-2 flex flex-wrap gap-2 text-[10px] uppercase tracking-[0.3em] text-slate-400">
                    {item.id ? (
                      <button onClick={() => reportComment(item)}>Пожаловаться</button>
                    ) : null}
                    {role === "IMMORTAL" ? (
                      <button onClick={() => removeComment(item)}>Удалить</button>
                    ) : null}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      ) : null}
    </article>
  );
}

function AdminPage() {
  const auth = authStorage.get();
  const role = getUserRole(auth);
  const username = auth?.username || "anon";
  const [incidentForm, setIncidentForm] = React.useState({
    title: "",
    description: "",
    place: "",
    department: "ГК",
    type: "DISRUPTION",
    occurredAt: "",
    offenderId: "",
    punishment: "NONE",
  });
  const [message, setMessage] = React.useState("");
  const [users, setUsers] = React.useState([]);
  const [usersError, setUsersError] = React.useState("");
  const [userFilter, setUserFilter] = React.useState("");
  const slotResult = slotStorage.get();

  const submitIncident = async (event) => {
    event.preventDefault();
    setMessage("");
    const payload = { ...incidentForm };
    if (slotResult?.isExpulsion) {
      payload.description = `${payload.description}\n\nНаказание: Отчисление`;
    }
    try {
      await apiFetch("/api/incidents", {
        method: "POST",
        body: JSON.stringify({
          ...payload,
          occurredAt: new Date(payload.occurredAt).toISOString(),
          punishment: payload.punishment,
          department: payload.department,
        }),
      });
      achievementsStorage.bump(username, "incidents");
      slotStorage.clear();
      setMessage("Инцидент создан.");
      setIncidentForm((prev) => ({ ...prev, title: "", description: "" }));
    } catch (err) {
      setMessage(err.message || "Ошибка создания инцидента");
    }
  };

  const loadUsers = React.useCallback(async () => {
    try {
      const list = await apiFetch("/api/users");
      setUsers(list || []);
      setUsersError("");
    } catch (err) {
      setUsersError(err.message || "Ошибка загрузки пользователей");
    }
  }, []);

  const updateRole = async (userId, nextRole) => {
    try {
      await apiFetch(`/api/users/${userId}/role`, {
        method: "PATCH",
        body: JSON.stringify({ role: nextRole }),
      });
      await loadUsers();
    } catch (err) {
      setUsersError(err.message || "Ошибка обновления роли");
    }
  };

  React.useEffect(() => {
    if (role === "ADMIN" || role === "IMMORTAL") {
      loadUsers();
    }
  }, [role, loadUsers]);

  return (
    <PageShell
      title="Админ-панель"
    >
      {role === "ADMIN" || role === "TEACHER" || role === "IMMORTAL" ? (
        <div className="grid gap-6 lg:grid-cols-2">
        {role === "TEACHER" || role === "ADMIN" || role === "IMMORTAL" ? (
          <form onSubmit={submitIncident} className="card-glass rounded-3xl p-6">
          <div className="flex items-center justify-between">
            <h3 className="font-display text-2xl text-neon">Новый инцидент</h3>
            {slotResult?.isExpulsion ? (
              <span className="chip rounded-full px-3 py-1 text-xs text-neon">
                Слот: 777 • Отчисление
              </span>
            ) : null}
          </div>
          <div className="mt-4 grid gap-3">
            <input
              placeholder="Название"
              className="rounded-2xl border border-white/10 bg-black/30 px-4 py-2 text-white"
              value={incidentForm.title}
              onChange={(event) =>
                setIncidentForm((prev) => ({ ...prev, title: event.target.value }))
              }
              required
            />
            <textarea
              placeholder="Описание"
              className="min-h-[120px] rounded-2xl border border-white/10 bg-black/30 px-4 py-2 text-white"
              value={incidentForm.description}
              onChange={(event) =>
                setIncidentForm((prev) => ({
                  ...prev,
                  description: event.target.value,
                }))
              }
              required
            />
            <div className="grid gap-3 md:grid-cols-2">
              <input
                placeholder="Место"
                className="rounded-2xl border border-white/10 bg-black/30 px-4 py-2 text-white"
                value={incidentForm.place}
                onChange={(event) =>
                  setIncidentForm((prev) => ({ ...prev, place: event.target.value }))
                }
                required
              />
              <select
                className="rounded-2xl border border-white/10 bg-black/30 px-4 py-2 text-white"
                value={incidentForm.department}
                onChange={(event) =>
                  setIncidentForm((prev) => ({ ...prev, department: event.target.value }))
                }
              >
                <option value="ГК">ГК</option>
                <option value="Биржа">Биржа</option>
                <option value="Ломо">Ломо</option>
                <option value="Чайка">Чайка</option>
                <option value="Гривцова">Гривцова</option>
                <option value="Хайпарк">Хайпарк</option>
                <option value="Вязьма">Вязьма</option>
                <option value="Б6">Б6</option>
                <option value="Ленсовета">Ленсовета</option>
                <option value="МСГ">МСГ</option>
              </select>
            </div>
            <div className="grid gap-3 md:grid-cols-2">
              <select
                className="rounded-2xl border border-white/10 bg-black/30 px-4 py-2 text-white"
                value={incidentForm.type}
                onChange={(event) =>
                  setIncidentForm((prev) => ({ ...prev, type: event.target.value }))
                }
              >
                <option value="DISRUPTION">DISRUPTION</option>
                <option value="DAMAGE">DAMAGE</option>
                <option value="CHEATING">CHEATING</option>
                <option value="AGGRESSION">AGGRESSION</option>
                <option value="OTHER">OTHER</option>
              </select>
              <select
                className="rounded-2xl border border-white/10 bg-black/30 px-4 py-2 text-white"
                value={incidentForm.punishment}
                onChange={(event) =>
                  setIncidentForm((prev) => ({ ...prev, punishment: event.target.value }))
                }
              >
                <option value="NONE">NONE</option>
                <option value="WARNING">WARNING</option>
                <option value="REPRIMAND">REPRIMAND</option>
                <option value="SUSPEND">SUSPEND</option>
                <option value="EXPULSION">EXPULSION</option>
              </select>
            </div>
            <div className="grid gap-3 md:grid-cols-2">
              <input
                type="datetime-local"
                className="rounded-2xl border border-white/10 bg-black/30 px-4 py-2 text-white"
                value={incidentForm.occurredAt}
                onChange={(event) =>
                  setIncidentForm((prev) => ({
                    ...prev,
                    occurredAt: event.target.value,
                  }))
                }
                required
              />
              <input
                placeholder="ISU нарушителя"
                className="rounded-2xl border border-white/10 bg-black/30 px-4 py-2 text-white"
                value={incidentForm.offenderId}
                onChange={(event) =>
                  setIncidentForm((prev) => ({
                    ...prev,
                    offenderId: event.target.value,
                  }))
                }
                required
              />
            </div>
            <button className="rounded-full bg-ember px-6 py-3 text-xs uppercase tracking-[0.3em] text-black">
              Создать
            </button>
          </div>
        </form>
        ) : (
          <div className="card-glass rounded-3xl p-6 text-sm text-slate-300">
            Создание нарушений доступно преподавателям, администраторам и бессмертным.
          </div>
        )}
        {role === "ADMIN" || role === "IMMORTAL" ? (
          <div className="card-glass rounded-3xl p-6 text-sm text-slate-300">
            <h3 className="font-display text-2xl text-neon">Роли пользователей</h3>
            <p className="mt-2 text-xs text-slate-400">
              Управление ролями доступно администратору и бессмертному.
            </p>
            {usersError ? (
              <p className="mt-4 text-xs text-ember">{usersError}</p>
            ) : null}
            <div className="mt-4">
              <input
                className="mb-3 w-full rounded-2xl border border-white/10 bg-black/30 px-4 py-2 text-sm text-white"
                placeholder="Поиск по ISU или имени"
                value={userFilter}
                onChange={(event) => setUserFilter(event.target.value.toLowerCase())}
              />
              <div className="space-y-3 max-h-[360px] overflow-y-auto pr-1">
              {users.length === 0 ? (
                <p className="text-xs text-slate-400">Нет пользователей</p>
              ) : (
                users
                  .filter((user) => {
                    if (!userFilter) return true;
                    return (
                      user.username.toLowerCase().includes(userFilter) ||
                      user.displayName.toLowerCase().includes(userFilter)
                    );
                  })
                  .map((user) => (
                  <div
                    key={user.id}
                    className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-white/5 bg-black/20 px-4 py-3"
                  >
                    <div>
                      <p className="text-sm text-white">{user.displayName}</p>
                      <p className="text-xs text-slate-500">
                        {user.username} • {user.role}
                      </p>
                    </div>
                    <select
                      className="rounded-full border border-white/10 bg-black/30 px-3 py-2 text-xs text-white"
                      value={user.role}
                      onChange={(event) =>
                        updateRole(user.id, event.target.value)
                      }
                    >
                      <option value="STUDENT">STUDENT</option>
                      <option value="TEACHER">TEACHER</option>
                      <option value="ADMIN">ADMIN</option>
                      <option value="IMMORTAL">IMMORTAL</option>
                    </select>
                  </div>
                ))
              )}
              </div>
            </div>
          </div>
        ) : null}
      </div>
      ) : (
        <div className="card-glass rounded-3xl p-8 text-slate-300">
          Этот раздел доступен только для ролей ADMIN и TEACHER.
        </div>
      )}
      {message ? (
        <div className="mt-6 rounded-2xl border border-white/10 bg-black/30 px-4 py-3 text-sm text-slate-200">
          {message}
        </div>
      ) : null}
    </PageShell>
  );
}

function SlotMachinePage() {
  const auth = authStorage.get();
  const role = getUserRole(auth);
  const [reels, setReels] = React.useState([0, 0, 0]);
  const [spinning, setSpinning] = React.useState(false);
  const [result, setResult] = React.useState(slotStorage.get());

  if (role !== "ADMIN" && role !== "TEACHER" && role !== "IMMORTAL") {
    return (
      <PageShell
        title="Слот-машина наказаний"
        subtitle="Доступно только для ролей ADMIN и TEACHER."
      >
        <div className="card-glass rounded-3xl p-8 text-slate-300">
          Недостаточно прав.
        </div>
      </PageShell>
    );
  }

  const spin = () => {
    if (spinning) return;
    setSpinning(true);
    const forceJackpot = Math.random() < 0.25;
    const next = forceJackpot
      ? [7, 7, 7]
      : [0, 0, 0].map(() => Math.floor(Math.random() * 10));
    setTimeout(() => {
      setReels(next);
      const isExpulsion = next.every((digit) => digit === 7);
      const payload = { value: next.join(""), isExpulsion };
      slotStorage.set(payload);
      setResult(payload);
      setSpinning(false);
    }, 900);
  };

  return (
    <PageShell
      title="Слот-машина наказаний"
      subtitle="Если выпадает 777, будет сюрприз!"
    >
      <div className="card-glass mx-auto max-w-3xl rounded-3xl p-8 text-center">
        <div className="slot-window mx-auto flex w-full max-w-xl items-center justify-center gap-4 rounded-3xl py-6">
          {reels.map((value, index) => (
            <div
              key={`reel-${index}`}
              className="slot-reel w-20 text-neon"
              style={{ transform: spinning ? "translateY(-10px)" : "translateY(0)" }}
            >
              {value}
            </div>
          ))}
        </div>
        <button
          onClick={spin}
          className="mt-8 rounded-full bg-neon px-8 py-3 text-xs uppercase tracking-[0.3em] text-black"
          disabled={spinning}
        >
          {spinning ? "Крутится..." : "Запустить"}
        </button>
        {result ? (
          <div className="mt-6 text-sm text-slate-300">
            Результат: <span className="text-neon">{result.value}</span>{" "}
            {result.isExpulsion ? "• Наказание: Отчисление" : "• Без наказания"}
          </div>
        ) : null}
      </div>
    </PageShell>
  );
}

function AchievementsPage() {
  const auth = authStorage.get();
  const role = getUserRole(auth);
  const username = auth?.username || "anon";
  const stats = achievementsStorage.get(username);
  const [selfIncidents, setSelfIncidents] = React.useState(0);
  const [selfExpelled, setSelfExpelled] = React.useState(false);

  React.useEffect(() => {
    if (!auth) return;
    apiFetch("/api/users/me")
      .then((me) => apiFetch(`/api/hooligans/${me.id}`))
      .then((card) => {
        const incidents = card?.incidents || [];
        setSelfIncidents(incidents.length);
        setSelfExpelled(
          incidents.some((incident) => (incident.punishment || "NONE") === "EXPULSION")
        );
      })
      .catch(() => {});
  }, [auth]);

  const achievements = [
    {
      title: "Лайк-джет",
      description: "Поставить 5 лайков.",
      done: stats.likes >= 5,
      progress: `${stats.likes}/5`,
    },
    {
      title: "Судья Дреддит",
      description: "Осудить 100 раз.",
      done: stats.dislikes >= 100,
      progress: `${stats.dislikes}/100`,
    },
    {
      title: "Коммент‑дрон",
      description: "Оставить 20 комментариев.",
      done: stats.comments >= 20,
      progress: `${stats.comments}/20`,
    },
    {
      title: "Теплый ламповый спор",
      description: "Оставить 50 комментариев.",
      done: stats.comments >= 50,
      progress: `${stats.comments}/50`,
    },
  ];

  if (role === "TEACHER" || role === "ADMIN") {
    achievements.push(
      {
        title: "Протоколист",
        description: "Создать 3 нарушения.",
        done: stats.incidents >= 3,
        progress: `${stats.incidents}/3`,
      },
      {
        title: "Архивариус проступков",
        description: "Создать 10 нарушений.",
        done: stats.incidents >= 10,
        progress: `${stats.incidents}/10`,
      },
      {
        title: "Каратель-менеджер",
        description: "Создать 20 нарушений.",
        done: stats.incidents >= 20,
        progress: `${stats.incidents}/20`,
      }
    );
  }

  if (role === "IMMORTAL") {
    achievements.push(
      {
        title: "Глас бессмертного",
        description: "Разослать 3 алерта.",
        done: stats.alerts >= 3,
        progress: `${stats.alerts}/3`,
      },
      {
        title: "Очиститель тьмы",
        description: "Удалить 5 токсичных комментариев.",
        done: stats.moderations >= 5,
        progress: `${stats.moderations}/5`,
      },
      {
        title: "Безотказный пейджер",
        description: "Разослать 10 алертов.",
        done: stats.alerts >= 10,
        progress: `${stats.alerts}/10`,
      }
    );
  }

  if (role === "STUDENT") {
    achievements.push(
      {
        title: "Первый косяк",
        description: "Получить своё первое нарушение.",
        done: selfIncidents >= 1,
        progress: `${selfIncidents}/1`,
      },
      {
        title: "Постоянный клиент",
        description: "Накопить 3 нарушения.",
        done: selfIncidents >= 3,
        progress: `${selfIncidents}/3`,
      },
      {
        title: "На грани отчисления",
        description: "Добраться до наказания EXPULSION.",
        done: selfExpelled,
        progress: selfExpelled ? "EXPULSION" : "нет",
      },
      {
        title: "Автомат в руки и вперёд",
        description: "Поймал отчисление.",
        done: selfExpelled,
        progress: selfExpelled ? "отчислен" : "нет",
      },
      {
        title: "Мамин бунтарь",
        description: "Собрать 3 проступка.",
        done: selfIncidents >= 3,
        progress: `${selfIncidents}/3`,
      },
      {
        title: "Сын маминой подруги",
        description: "Осудить 5 раз.",
        done: stats.dislikes >= 5,
        progress: `${stats.dislikes}/5`,
      }
    );
  }

  return (
    <PageShell
      title="Ачивки активности"
      subtitle="Смешные бейджи за реакции, комментарии и модерацию."
    >
      <div className="grid gap-6 md:grid-cols-2">
        {achievements.map((item) => (
          <div key={item.title} className="card-glass rounded-3xl p-6">
            <p className="text-xs uppercase tracking-[0.3em] text-slate-400">
              {item.done ? "Разблокировано" : "В процессе"}
            </p>
            <h3 className="mt-2 text-2xl font-semibold text-white">
              {item.title}
            </h3>
            <p className="mt-2 text-sm text-slate-300">{item.description}</p>
            <div className="mt-4 flex items-center justify-between text-xs text-slate-400">
              <span>Прогресс</span>
              <span className="text-neon">{item.progress}</span>
            </div>
          </div>
        ))}
      </div>
    </PageShell>
  );
}

function StatsPage() {
  const [range, setRange] = React.useState({
    from: "",
  });
  const { data, loading, error, run } = useFetchState([]);
  const [boardTop, setBoardTop] = React.useState([]);
  const [boardAll, setBoardAll] = React.useState([]);

  const loadStats = async () => {
    const params = new URLSearchParams();
    if (range.from) params.set("from", new Date(range.from).toISOString());
    return apiFetch(`/api/incidents?${params.toString()}`);
  };

  React.useEffect(() => {
    run(loadStats);
  }, []);

  const stats = React.useMemo(() => {
    const items = Array.isArray(data) ? data : [];
    const byType = items.reduce((acc, item) => {
      acc[item.type] = (acc[item.type] || 0) + 1;
      return acc;
    }, {});
    const byDay = items.reduce((acc, item) => {
      const day = new Date(item.occurredAt).toLocaleDateString();
      acc[day] = (acc[day] || 0) + 1;
      return acc;
    }, {});
    const offenders = items.reduce((acc, item) => {
      const list = acc.get(item.offenderId) || [];
      list.push(item);
      acc.set(item.offenderId, list);
      return acc;
    }, new Map());
    let expelledCount = 0;
    let incidentsToExpulsionTotal = 0;
    offenders.forEach((list) => {
      const expulsion = list.find((i) => (i.punishment || "NONE") === "EXPULSION");
      if (expulsion) {
        expelledCount += 1;
        const countToExpulsion = list.filter(
          (i) =>
            new Date(i.occurredAt) <= new Date(expulsion.occurredAt)
        ).length;
        incidentsToExpulsionTotal += countToExpulsion;
      }
    });
    const expelledPercent =
      offenders.size === 0 ? 0 : Math.round((expelledCount / offenders.size) * 100);
    const avgToExpulsion =
      expelledCount === 0 ? 0 : (incidentsToExpulsionTotal / expelledCount).toFixed(1);

    const facultyShare = boardAll.reduce((acc, entry) => {
      const key = entry.faculty || "N/A";
      acc[key] = (acc[key] || 0) + (entry.incidentsCount || 0);
      return acc;
    }, {});
    const groupShare = boardAll.reduce((acc, entry) => {
      const key = entry.groupName || "N/A";
      acc[key] = (acc[key] || 0) + (entry.incidentsCount || 0);
      return acc;
    }, {});
    const incidentsTotalForShare = Object.values(facultyShare).reduce(
      (sum, val) => sum + val,
      0
    );

    return {
      byType,
      byDay,
      total: items.length,
      expelledPercent,
      avgToExpulsion,
      offendersCount: offenders.size,
      facultyShare,
      groupShare,
      incidentsTotalForShare,
    };
  }, [data, boardAll]);

  const maxTypeValue = Math.max(1, ...Object.values(stats.byType));
  const maxDayValue = Math.max(1, ...Object.values(stats.byDay));

  React.useEffect(() => {
    apiFetch("/api/hooligans/board?limit=3")
      .then(setBoardTop)
      .catch(() => setBoardTop([]));
    apiFetch("/api/hooligans/board?limit=500")
      .then(setBoardAll)
      .catch(() => setBoardAll([]));
  }, []);

  return (
    <PageShell
      title="Статистика"
    >
      <div className="card-glass rounded-3xl p-6">
        <div className="flex flex-wrap items-center gap-3">
          <input
            type="datetime-local"
            className="rounded-full border border-white/10 bg-black/30 px-4 py-2 text-xs text-white"
            value={range.from}
            onChange={(event) =>
              setRange((prev) => ({ ...prev, from: event.target.value }))
            }
          />
          <button
            className="rounded-full bg-mint px-6 py-2 text-xs uppercase tracking-[0.3em] text-black"
            onClick={() => run(loadStats)}
          >
            Обновить
          </button>
          <span className="text-xs text-slate-400">
            Всего: {stats.total}
          </span>
        </div>
        {loading ? <p className="mt-4 text-sm">Загрузка...</p> : null}
        {error ? <p className="mt-4 text-sm text-ember">{error}</p> : null}
      </div>
      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <div className="card-glass rounded-3xl p-6">
          <h3 className="font-display text-2xl text-neon">Типы нарушений</h3>
          <div className="mt-4 space-y-3">
            {Object.keys(stats.byType).length === 0 ? (
              <p className="text-sm text-slate-300">Нет данных</p>
            ) : (
              Object.entries(stats.byType).map(([type, value]) => (
                <div key={type}>
                  <div className="flex justify-between text-xs text-slate-400">
                    <span>{type}</span>
                    <span>{value}</span>
                  </div>
                  <div className="mt-2 h-3 rounded-full bg-black/40">
                    <div
                      className="chart-bar h-3 rounded-full"
                      style={{ width: `${(value / maxTypeValue) * 100}%` }}
                    />
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
        <div className="card-glass rounded-3xl p-6">
          <h3 className="font-display text-2xl text-neon">
            Динамика по времени
          </h3>
          <div className="mt-4 space-y-3">
            {Object.keys(stats.byDay).length === 0 ? (
              <p className="text-sm text-slate-300">Нет данных</p>
            ) : (
              Object.entries(stats.byDay).map(([day, value]) => (
                <div key={day}>
                  <div className="flex justify-between text-xs text-slate-400">
                    <span>{day}</span>
                    <span>{value}</span>
                  </div>
                  <div className="mt-2 h-3 rounded-full bg-black/40">
                    <div
                      className="chart-bar h-3 rounded-full"
                      style={{ width: `${(value / maxDayValue) * 100}%` }}
                    />
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <div className="card-glass rounded-3xl p-6">
          <h3 className="font-display text-2xl text-neon">
            Лидеры рейтинга (топ 3)
          </h3>
          <div className="mt-4 space-y-3 text-sm text-slate-300">
            {boardTop.length === 0 ? (
              <p>Нет данных</p>
            ) : (
              boardTop.map((entry) => (
                <div
                  key={entry.userId}
                  className="flex items-center justify-between rounded-2xl border border-white/5 bg-black/20 px-4 py-3"
                >
                  <div>
                    <p>{entry.displayName}</p>
                    <p className="text-xs text-slate-500">
                      {entry.faculty || "Факультет N/A"} • {entry.groupName || "Группа N/A"}
                    </p>
                  </div>
                  <span className="text-neon">{entry.points.toFixed(1)}</span>
                </div>
              ))
            )}
          </div>
        </div>
        <div className="card-glass rounded-3xl p-6">
          <h3 className="font-display text-2xl text-neon">
            Отчисления
          </h3>
          <div className="mt-4 space-y-3 text-sm text-slate-300">
            <div className="flex items-center justify-between rounded-2xl border border-white/5 bg-black/20 px-4 py-3">
              <span>Процент отчисленных</span>
              <span className="text-neon">{stats.expelledPercent}%</span>
            </div>
            <div className="flex items-center justify-between rounded-2xl border border-white/5 bg-black/20 px-4 py-3">
              <span>Среднее число нарушений до отчисления</span>
              <span className="text-neon">{stats.avgToExpulsion}</span>
            </div>
            <div className="flex items-center justify-between rounded-2xl border border-white/5 bg-black/20 px-4 py-3">
              <span>Уникальных нарушителей</span>
              <span className="text-neon">{stats.offendersCount}</span>
            </div>
          </div>
        </div>
      </div>
      <div className="mt-6 card-glass rounded-3xl p-6">
        <h3 className="font-display text-2xl text-neon">
          Распределение по факультетам/группам
        </h3>
        <div className="mt-4 grid gap-4 md:grid-cols-2">
          <div className="rounded-2xl border border-white/5 bg-black/20 px-4 py-3 text-sm text-slate-300">
            <p className="text-xs uppercase tracking-[0.3em] text-slate-500">
              Факультеты
            </p>
            <div className="mt-2 space-y-2">
              {stats.incidentsTotalForShare === 0 ? (
                <p>Нет данных</p>
              ) : (
                Object.entries(stats.facultyShare).map(([faculty, value]) => (
                  <div key={faculty} className="flex justify-between text-xs text-slate-400">
                    <span>{faculty}</span>
                    <span className="text-neon">
                      {Math.round((value / stats.incidentsTotalForShare) * 100)}%
                    </span>
                  </div>
                ))
              )}
            </div>
          </div>
          <div className="rounded-2xl border border-white/5 bg-black/20 px-4 py-3 text-sm text-slate-300">
            <p className="text-xs uppercase tracking-[0.3em] text-slate-500">
              Группы
            </p>
            <div className="mt-2 space-y-2">
              {stats.incidentsTotalForShare === 0 ? (
                <p>Нет данных</p>
              ) : (
                Object.entries(stats.groupShare).map(([group, value]) => (
                  <div key={group} className="flex justify-between text-xs text-slate-400">
                    <span>{group}</span>
                    <span className="text-neon">
                      {Math.round((value / stats.incidentsTotalForShare) * 100)}%
                    </span>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </PageShell>
  );
}

function ImmortalPage() {
  const auth = authStorage.get();
  const role = getUserRole(auth);
  const username = auth?.username || "anon";
  const [message, setMessage] = React.useState("");
  const [alertText, setAlertText] = React.useState("");
  const [reports, setReports] = React.useState([]);

  const sendAlert = (event) => {
    event.preventDefault();
    if (!alertText.trim()) return;
    apiFetch("/api/alerts", {
      method: "POST",
      body: JSON.stringify({ message: alertText.trim() }),
    })
      .then((created) => {
        achievementsStorage.bump(username, "alerts");
        if (created) {
          window.dispatchEvent(new CustomEvent("alert:new", { detail: created }));
        }
        setAlertText("");
        setMessage("Алерт отправлен всем пользователям.");
      })
      .catch((err) => setMessage(err.message || "Не удалось отправить алерт"));
  };

  const resolveReport = (report) => {
    reportsApi
      .remove(report.id)
      .then(() => {
        setReports((prev) => prev.filter((item) => item.id !== report.id));
        setMessage("Жалоба закрыта.");
      })
      .catch((err) => setMessage(err.message || "Ошибка удаления жалобы"));
  };

  const deleteComment = (report) => {
    apiFetch(`/api/incidents/${report.incidentId}/comments/${report.commentId}`, {
      method: "DELETE",
    })
      .then(() => reportsApi.remove(report.id))
      .then(() => {
        achievementsStorage.bump(username, "moderations");
        setReports((prev) => prev.filter((item) => item.id !== report.id));
        setMessage("Комментарий удалён.");
      })
      .catch((err) => setMessage(err.message || "Ошибка удаления"));
  };

  React.useEffect(() => {
    reportsApi
      .list()
      .then((list) => setReports(list || []))
      .catch(() => setReports([]));
  }, []);

  if (role !== "IMMORTAL") {
    return (
      <PageShell
        title="Бессмертный"
        subtitle="Раздел доступен только для роли IMMORTAL."
      >
        <div className="card-glass rounded-3xl p-8 text-slate-300">
          Недостаточно прав.
        </div>
      </PageShell>
    );
  }

  return (
    <PageShell
      title="Бессмертный"
      subtitle="Создание алертов и модерация контента."
    >
      <div className="grid gap-6 lg:grid-cols-[1fr_1.2fr]">
        <form onSubmit={sendAlert} className="card-glass rounded-3xl p-6">
          <h3 className="font-display text-2xl text-neon">Срочный алерт</h3>
          <p className="mt-2 text-xs text-slate-400">
            Пример: «Студент вошёл в аудиторию в куртке. Исправить срочно.»
          </p>
          <textarea
            className="mt-4 min-h-[140px] w-full rounded-2xl border border-white/10 bg-black/30 px-4 py-3 text-white"
            value={alertText}
            onChange={(event) => setAlertText(event.target.value)}
            required
          />
          <button className="mt-4 w-full rounded-full bg-neon px-6 py-3 text-xs uppercase tracking-[0.3em] text-black">
            Отправить всем
          </button>
        </form>
        <div className="card-glass rounded-3xl p-6">
          <h3 className="font-display text-2xl text-neon">Жалобы на контент</h3>
          <div className="mt-4 space-y-3 text-sm text-slate-300">
            {reports.length === 0 ? (
              <p>Жалоб нет.</p>
            ) : (
              reports.map((report) => (
                <div
                  key={report.id}
                  className="rounded-2xl border border-white/5 bg-black/20 px-4 py-3"
                >
                  <p className="text-xs text-slate-500">
                    Инцидент: {report.incidentId}
                  </p>
                  <p className="mt-2 text-xs uppercase tracking-[0.3em] text-slate-500">
                    {report.commentAuthorDisplayName}
                  </p>
                  <p className="mt-2">{report.commentText}</p>
                  {report.reason ? (
                    <p className="mt-2 text-xs text-slate-400">
                      Причина: {report.reason}
                    </p>
                  ) : null}
                  <div className="mt-3 flex flex-wrap gap-2 text-[10px] uppercase tracking-[0.3em] text-slate-400">
                    <button onClick={() => deleteComment(report)}>
                      Удалить
                    </button>
                    <button onClick={() => resolveReport(report)}>
                      Отклонить
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
      {message ? (
        <div className="mt-6 rounded-2xl border border-white/10 bg-black/30 px-4 py-3 text-sm text-slate-200">
          {message}
        </div>
      ) : null}
    </PageShell>
  );
}

function HooliganPage() {
  const { id } = useParams();
  const { data, loading, error, run } = useFetchState(null);

  React.useEffect(() => {
    run(() => apiFetch(`/api/hooligans/${id}`));
  }, [id, run]);

  return (
    <PageShell
      title="Карточка нарушителя"
      subtitle="История нарушений, реакции и баллы."
    >
      <div className="card-glass rounded-3xl p-6">
        {loading ? (
          <p>Загрузка...</p>
        ) : error ? (
          <p className="text-ember">{error}</p>
        ) : data ? (
          <div>
            <div className="flex flex-wrap items-center justify-between gap-4">
              <div>
                <p className="text-xs uppercase tracking-[0.3em] text-slate-400">
                  {data.user.role}
                </p>
                <h3 className="mt-2 text-3xl font-semibold text-white">
                  {data.user.displayName}
                </h3>
                <p className="text-sm text-slate-300">
                  {data.user.faculty} {data.user.groupName}
                </p>
              </div>
              <div className="text-right">
                <p className="text-sm text-slate-400">Баллы</p>
                <p className="text-3xl font-semibold text-neon">
                  {data.points.toFixed(1)}
                </p>
              </div>
            </div>
            <div className="mt-4 flex flex-wrap gap-3 text-xs text-slate-400">
              <span className="chip rounded-full px-3 py-1">Лайки: {data.likes}</span>
              <span className="chip rounded-full px-3 py-1">
                Дизлайки: {data.dislikes}
              </span>
              <span className="chip rounded-full px-3 py-1">
                Инциденты: {data.incidents.length}
              </span>
            </div>
      <div className="mt-6 space-y-3">
            {data.incidents.map((incident) => (
                <div
                  key={incident.id}
                  className="rounded-2xl border border-white/5 bg-black/20 px-4 py-3"
                >
                  <div className="flex items-center justify-between text-sm text-slate-300">
                    <span>{incident.title}</span>
                    <span className="text-xs text-slate-500">{incident.type}</span>
                  </div>
                  <p className="mt-1 text-xs text-slate-500">
                    {new Date(incident.createdAt).toLocaleString()} •{" "}
                    {incident.moderationStatus} • {incident.punishment || "NONE"}
                  </p>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <p>Нет данных</p>
        )}
      </div>
    </PageShell>
  );
}

function HomeRedirect() {
  const navigate = useNavigate();
  React.useEffect(() => {
    const auth = authStorage.get();
    if (auth) {
      navigate("/incidents");
    } else {
      navigate("/login");
    }
  }, [navigate]);
  return null;
}

function RequireAuth({ children }) {
  const auth = authStorage.get();
  if (!auth) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

export default function App() {
  return (
    <HashRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<HomeRedirect />} />
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/incidents"
            element={
              <RequireAuth>
                <IncidentsPage />
              </RequireAuth>
            }
          />
          <Route
            path="/admin"
            element={
              <RequireAuth>
                <AdminPage />
              </RequireAuth>
            }
          />
          <Route
            path="/slot"
            element={
              <RequireAuth>
                <SlotMachinePage />
              </RequireAuth>
            }
          />
          <Route
            path="/stats"
            element={
              <RequireAuth>
                <StatsPage />
              </RequireAuth>
            }
          />
          <Route
            path="/achievements"
            element={
              <RequireAuth>
                <AchievementsPage />
              </RequireAuth>
            }
          />
          <Route
            path="/immortal"
            element={
              <RequireAuth>
                <ImmortalPage />
              </RequireAuth>
            }
          />
          <Route
            path="/hooligan/:id"
            element={
              <RequireAuth>
                <HooliganPage />
              </RequireAuth>
            }
          />
        </Routes>
      </Layout>
    </HashRouter>
  );
}
