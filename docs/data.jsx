// data.jsx — mock data for the prototype

const CELEBS = [
  { id: 'c1', name: 'Lila Okafor',     handle: '@lilaokafor',   field: 'Music',     hue: 12,  bio: 'Grammy-nominated. Touring "Northbound" through autumn.', verified: true,  followers: '2.4M', signed: 481,  price: 24, sig: 'curl',  remaining: 12 },
  { id: 'c2', name: 'Marco Vidal',     handle: '@marcovidal',   field: 'Football', hue: 200, bio: 'Striker, Atlético FC. Cap #9. Tuesday signings only.',   verified: true,  followers: '5.1M', signed: 1280, price: 32, sig: 'fast',  remaining: 4  },
  { id: 'c3', name: 'Yuna Park',       handle: '@yunapark',     field: 'Film',      hue: 320, bio: 'Actor. "After the Frost" out now. Director by accident.', verified: true,  followers: '880K', signed: 215,  price: 28, sig: 'loop',  remaining: 0  },
  { id: 'c4', name: 'Drew Halloway',   handle: '@drewh',        field: 'Comedy',    hue: 80,  bio: 'Stand-up. Sketch. Sometimes both at once. NYC.',         verified: true,  followers: '420K', signed: 92,   price: 18, sig: 'curl',  remaining: 25 },
  { id: 'c5', name: 'Ines Carvalho',   handle: '@inesc',        field: 'Tennis',    hue: 150, bio: 'WTA tour. Lefty backhand. Espresso devotee.',           verified: true,  followers: '1.1M', signed: 612,  price: 26, sig: 'fast',  remaining: 8  },
  { id: 'c6', name: 'Theo Andersen',   handle: '@theoand',      field: 'Author',    hue: 260, bio: '"The Quiet Atlas" — NYT bestseller. Cats: 2.',          verified: true,  followers: '210K', signed: 58,   price: 14, sig: 'loop',  remaining: 18 },
];

const DROPS = [
  { id: 'd1', celeb: 'c1', title: 'Northbound — Tour Print',     scene: 'sunset', edition: '042/365', when: '2h ago', likes: 1240, locked: false, rarity: 'rare' },
  { id: 'd2', celeb: 'c2', title: 'Match Day — Stamp 0009',      scene: 'stage',  edition: '009/365', when: '4h ago', likes: 3402, locked: true,  rarity: 'common' },
  { id: 'd3', celeb: 'c3', title: 'After the Frost · Premiere',  scene: 'cool',   edition: '111/250', when: 'Today',  likes: 988,  locked: false, rarity: 'rare' },
  { id: 'd4', celeb: 'c5', title: 'Roland — Match Point',        scene: 'studio', edition: '317/500', when: 'Yest.',  likes: 2104, locked: false, rarity: 'common' },
];

const COLLECTION = [
  { id: 'k1', celeb: 'c1', edition: '042/365', date: 'May 2', scene: 'sunset', rarity: 'rare', mine: true },
  { id: 'k2', celeb: 'c2', edition: '109/365', date: 'Apr 28',scene: 'stage',  rarity: 'common', mine: true },
  { id: 'k3', celeb: 'c4', edition: '008/500', date: 'Apr 14',scene: 'studio', rarity: 'common', mine: true },
  { id: 'k4', celeb: 'c6', edition: '022/200', date: 'Apr 02',scene: 'cool',   rarity: 'legend', mine: true },
  { id: 'k5', celeb: 'c5', edition: '317/500', date: 'Mar 30',scene: 'studio', rarity: 'common', mine: true },
];

const QUEUE = [
  { id: 'q1', fan: 'Eli J.',     msg: 'Loved Northbound — first concert was you in Atlanta!', when: '6 min', price: 24, mood: 'pending' },
  { id: 'q2', fan: 'Saanvi M.',  msg: 'For my mom\'s birthday — she sings every track.',     when: '14 min', price: 24, mood: 'pending' },
  { id: 'q3', fan: 'Tomás R.',   msg: 'Met you backstage in Berlin (briefly!)',              when: '32 min', price: 24, mood: 'accepted' },
  { id: 'q4', fan: 'Mae K.',     msg: 'New collector. This would mean the world.',           when: '1 hr',   price: 24, mood: 'pending' },
];

function celebById(id) { return CELEBS.find(c => c.id === id); }

Object.assign(window, { CELEBS, DROPS, COLLECTION, QUEUE, celebById });
