export {};

interface Clock {
  now(): string;
}

interface Mailer {
  send(to: string, text: string): void;
}

class ReminderService {
  sent = 0;

  constructor(private readonly clock: Clock, private readonly mailer: Mailer) {}

  remind(user: string): void {
    this.mailer.send(user, `[${this.clock.now()}] rappel envoye a ${user}`);
    this.sent += 1;
  }
}

const messages: string[] = [];
const fixedClock: Clock = { now: () => '2024-03-01T09:00:00Z' };
const memoryMailer: Mailer = { send: (_to, text) => void messages.push(text) };
const service = new ReminderService(fixedClock, memoryMailer);
service.remind('ada@dlr.dev');
console.log(messages[0]);
console.log(`Messages: ${messages.length}`);
console.log(`Rappels envoyes: ${service.sent}`);
