// Von Lukas bearbeitet
// Feature models for the Teacher view (frontend-only mocks)

export interface Student {
  id: string;
  name: string;
  classId?: string;
  className: string;
  avatarUrl?: string;
}

export interface Project {
  id: string;
  name: string;
}

export interface Assignment {
  studentId: string;
  projectId: string;
  assigned: boolean;
}

export interface Scores {
  teacher: string;
  peer: string;
  self: string;
  trendTeacher: string;
  trendPeer: string;
  trendSelf: string;
}

export interface ProjectOption {
  label: string;
  value: string;
}

export interface ClassOption {
  id: string;
  name: string;
}

// Notes sent by teachers to students
export interface Note {
  toStudentId: string;
  projectId: string;
  text: string;
  createdAt: string; // ISO timestamp
}

// Peer evaluation submitted by a student about another student
export interface PeerEvaluation {
  fromStudentId: string;
  toStudentId: string;
  projectId: string;
  grade: number; // numeric grade like 1.0 .. 6.0
  createdAt: string; // ISO timestamp
}
