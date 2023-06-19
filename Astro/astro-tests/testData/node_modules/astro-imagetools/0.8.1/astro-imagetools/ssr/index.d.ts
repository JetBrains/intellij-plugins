import type { IncomingMessage, ServerResponse } from "http";

export function middleware(
  request: IncomingMessage,
  response: ServerResponse
): Buffer;
