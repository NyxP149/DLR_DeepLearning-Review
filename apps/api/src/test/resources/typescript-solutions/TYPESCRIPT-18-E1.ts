export {};

interface Clock {
  now(): string;
}

interface Logger {
  info(message: string): void;
}

class InvoiceService {
  private issued = 0;
  private clock: Clock;
  private logger: Logger;

  constructor(clock: Clock, logger: Logger) {
    this.clock = clock;
    this.logger = logger;
  }

  issue(id: number, total: number): string {
    const message = `[${this.clock.now()}] facture ${id} émise (total ${total})`;
    this.logger.info(message);
    this.issued++;
    return message;
  }

  get count(): number {
    return this.issued;
  }
}

class FixedClock implements Clock {
  now(): string {
    return '2024-01-01T00:00:00Z';
  }
}

class MemoryLogger implements Logger {
  messages: string[] = [];

  info(message: string): void {
    this.messages.push(message);
  }
}

const logger = new MemoryLogger();
const service = new InvoiceService(new FixedClock(), logger);
console.log(service.issue(42, 120));
console.log(`Journal: ${logger.messages.length} message(s)`);
console.log(`Émises: ${service.count}`);
