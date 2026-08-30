export interface LabContent {
  code: string;
  language: string;
  number: number;
  slug: string;
  title: string;
  difficulty: string;
  threshold: number;
  activityType: 'LAB' | 'PROJECT';
  prerequisites: string[];
  objectives: string[];
  sections: LessonSection[];
  keyConcepts: KeyConcept[];
  exercises: Exercise[];
  quiz: QuizQuestion[];
  checklist: string[];
}

export interface LessonSection {
  title: string;
  content: string;
  conceptCodes: string[];
}

export interface KeyConcept {
  code: string;
  name: string;
  definition: string;
  whyExists: string;
  whyImportant: string;
  minimalExample: string;
  commonMistake: string;
  masteryQuestion: string;
  masteryProof: string;
}

export interface Exercise {
  code: string;
  title: string;
  statement: string;
  starterCode: string;
}

export interface QuizQuestion {
  code: string;
  type: 'SINGLE_CHOICE' | 'FREE_TEXT';
  prompt: string;
  choices: string[];
}
