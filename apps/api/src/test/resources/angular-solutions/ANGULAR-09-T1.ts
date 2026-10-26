export {};

interface Ticket { id: number; agent: string; priority: 'HIGH' | 'MEDIUM' | 'LOW'; status: 'OPEN' | 'RESOLVED'; hours: number; }

const tickets: Ticket[] = [
  { id: 1, agent: 'Ada', priority: 'HIGH', status: 'RESOLVED', hours: 4 },
  { id: 2, agent: 'Nyx', priority: 'LOW', status: 'RESOLVED', hours: 9 },
  { id: 3, agent: 'Ada', priority: 'MEDIUM', status: 'OPEN', hours: 0 },
  { id: 4, agent: 'Nyx', priority: 'HIGH', status: 'RESOLVED', hours: 2 },
  { id: 5, agent: 'Ada', priority: 'HIGH', status: 'RESOLVED', hours: 6 },
];

const resolved = (items: readonly Ticket[]): Ticket[] => items.filter((ticket) => ticket.status === 'RESOLVED');
const averageHours = (items: readonly Ticket[]): number =>
  Math.round(resolved(items).reduce((total, ticket) => total + ticket.hours, 0) / resolved(items).length);
const byPriority = (items: readonly Ticket[]): string =>
  (['HIGH', 'MEDIUM', 'LOW'] as const).map((priority) => `${priority}=${items.filter((t) => t.priority === priority).length}`).join(', ');
function topAgent(items: readonly Ticket[]): [string, number] {
  const counts = new Map<string, number>();
  for (const ticket of resolved(items)) counts.set(ticket.agent, (counts.get(ticket.agent) ?? 0) + 1);
  return [...counts.entries()].sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))[0];
}

const [agent, count] = topAgent(tickets);
console.log(`Resolus: ${resolved(tickets).length}`);
console.log(`Duree moyenne: ${averageHours(tickets)} h`);
console.log(`Par priorite: ${byPriority(tickets)}`);
console.log(`Meilleur agent: ${agent} (${count})`);
