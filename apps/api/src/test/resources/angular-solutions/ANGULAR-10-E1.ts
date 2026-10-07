export {};

interface Row {
  id: number;
  label: string;
}

const before: Row[] = [
  { id: 1, label: 'A' },
  { id: 2, label: 'B' },
  { id: 3, label: 'C' },
];
const after: Row[] = [
  { id: 2, label: 'B' },
  { id: 3, label: 'C2' },
  { id: 4, label: 'D' },
];

interface Diff {
  created: number;
  removed: number;
  updated: number;
  kept: number;
}

function diffWithoutTrackBy(oldRows: Row[], newRows: Row[]): Diff {
  return { created: newRows.length, removed: oldRows.length, updated: 0, kept: 0 };
}

function diffWithTrackBy(oldRows: Row[], newRows: Row[]): Diff {
  const oldById = new Map(oldRows.map((row) => [row.id, row] as const));
  const newIds = new Set(newRows.map((row) => row.id));
  const diff: Diff = { created: 0, removed: 0, updated: 0, kept: 0 };
  for (const row of newRows) {
    const previous = oldById.get(row.id);
    if (!previous) {
      diff.created++;
    } else if (previous.label !== row.label) {
      diff.updated++;
    } else {
      diff.kept++;
    }
  }
  for (const row of oldRows) {
    if (!newIds.has(row.id)) {
      diff.removed++;
    }
  }
  return diff;
}

function operations(diff: Diff): number {
  return diff.created + diff.removed + diff.updated;
}

const naive = diffWithoutTrackBy(before, after);
const tracked = diffWithTrackBy(before, after);
console.log(`Sans trackBy: supprimés=${naive.removed} créés=${naive.created} mis à jour=${naive.updated} conservés=${naive.kept}`);
console.log(`Avec trackBy: supprimés=${tracked.removed} créés=${tracked.created} mis à jour=${tracked.updated} conservés=${tracked.kept}`);
console.log(`Opérations DOM évitées: ${operations(naive) - operations(tracked)}`);
