const BASE_URL = 'http://localhost:8080/ctf-platform';

const getToken = () => localStorage.getItem('token');

const headers = (auth = true) => ({
  'Content-Type': 'application/json',
  ...(auth && getToken() ? { Authorization: `Bearer ${getToken()}` } : {}),
});

const handleResponse = async (response) => {
  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    return response.json();
  }
  const text = await response.text();
  if (!response.ok) {
    return { error: `Error ${response.status}: ${response.statusText || 'Unknown error'}` };
  }
  return { message: text };
};

const api = {
  // Auth
  login: (username, password) =>
    fetch(`${BASE_URL}/auth/login`, {
      method: 'POST', headers: headers(false),
      body: JSON.stringify({ username, password }),
    }).then(handleResponse).catch(err => ({ error: "Network error: Connection refused or CORS issue." })),

  register: (username, email, password) =>
    fetch(`${BASE_URL}/auth/register`, {
      method: 'POST', headers: headers(false),
      body: JSON.stringify({ username, email, password }),
    }).then(handleResponse).catch(err => ({ error: "Network error: Connection refused or CORS issue." })),

  // User
  getMe: () =>
    fetch(`${BASE_URL}/api/users/me`, { headers: headers() }).then(handleResponse),

  updateMe: (data) =>
    fetch(`${BASE_URL}/api/users/me`, {
      method: 'PUT', headers: headers(),
      body: JSON.stringify(data),
    }).then(handleResponse),

  getAllUsers: () =>
    fetch(`${BASE_URL}/api/admin/users`, { headers: headers() }).then(handleResponse),

  // Competitions
  getCompetitions: () =>
    fetch(`${BASE_URL}/api/competitions`, { headers: headers() }).then(handleResponse),

  getCompetition: (id) =>
    fetch(`${BASE_URL}/api/competitions/${id}`, { headers: headers() }).then(handleResponse),

  createCompetition: (data) =>
    fetch(`${BASE_URL}/api/competitions`, {
      method: 'POST', headers: headers(),
      body: JSON.stringify(data),
    }).then(handleResponse),

  deleteCompetition: (id) =>
    fetch(`${BASE_URL}/api/competitions/${id}`, {
      method: 'DELETE', headers: headers(),
    }).then(handleResponse),

  // Challenges
  getChallenges: (competitionId) =>
    fetch(`${BASE_URL}/api/challenges?competitionId=${competitionId}`, { headers: headers() }).then(handleResponse),

  getChallenge: (id) =>
    fetch(`${BASE_URL}/api/challenges/${id}`, { headers: headers() }).then(handleResponse),

  createChallenge: (data) =>
    fetch(`${BASE_URL}/api/challenges`, {
      method: 'POST', headers: headers(),
      body: JSON.stringify(data),
    }).then(handleResponse),

  deleteChallenge: (id) =>
    fetch(`${BASE_URL}/api/challenges/${id}`, {
      method: 'DELETE', headers: headers(),
    }).then(handleResponse),

  // Hints
  getHints: (challengeId) =>
    fetch(`${BASE_URL}/api/hints?challengeId=${challengeId}`, { headers: headers() }).then(handleResponse),

  // Submissions
  submitFlag: (challengeId, flag) =>
    fetch(`${BASE_URL}/api/submissions`, {
      method: 'POST', headers: headers(),
      body: JSON.stringify({ challengeId, flag }),
    }).then(handleResponse),

  // Scoreboard
  getScoreboard: (type = 'user') =>
    fetch(`${BASE_URL}/api/scoreboard${type === 'team' ? '?type=team' : ''}`, { headers: headers() }).then(handleResponse),

  // Teams
  getTeam: (id) =>
    fetch(`${BASE_URL}/api/teams/${id}`, { headers: headers() }).then(handleResponse),

  createTeam: (name, competitionId) =>
    fetch(`${BASE_URL}/api/teams`, {
      method: 'POST', headers: headers(),
      body: JSON.stringify({ name, competitionId }),
    }).then(handleResponse),

  joinTeam: (teamId) =>
    fetch(`${BASE_URL}/api/teams/join`, {
      method: 'POST', headers: headers(),
      body: JSON.stringify({ teamId }),
    }).then(handleResponse),

  leaveTeam: () =>
    fetch(`${BASE_URL}/api/teams/leave`, {
      method: 'POST', headers: headers(),
    }).then(handleResponse),
};

export default api;
