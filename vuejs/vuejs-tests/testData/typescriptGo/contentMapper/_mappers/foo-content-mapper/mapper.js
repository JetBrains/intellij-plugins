// A content mapper for the `.foo` files. Each `name = value` line becomes `export const name = value;`.
// The name and the value map back verbatim, so the diagnostics on them map to the `.foo` file.
let input = Buffer.alloc(0);

function writeMessage(message) {
  const body = Buffer.from(JSON.stringify(message), "utf8");
  process.stdout.write("Content-Length: " + body.length + "\r\n\r\n");
  process.stdout.write(body);
}

function transform(content) {
  let text = "";
  const mappings = [];
  let offset = 0;
  for (const line of content.split("\n")) {
    const match = /^(\s*)([A-Za-z_$][\w$]*)(\s*=\s*)(.*?)\s*$/.exec(line);
    if (match) {
      const nameStart = offset + match[1].length;
      const valueStart = nameStart + match[2].length + match[3].length;
      text += "export const ";
      mappings.push([text.length, match[2].length, nameStart, match[2].length, 0]);
      text += match[2] + " = ";
      mappings.push([text.length, match[4].length, valueStart, match[4].length, 0]);
      text += match[4] + ";\n";
    }
    offset += line.length + 1;
  }
  return {text, extension: ".ts", mappings};
}

function handleRequest(request) {
  switch (request.method) {
    case "initialize":
      return {positionEncoding: "utf-16", diagnosticSource: "foo-content-mapper"};
    case "openProject":
      return {configIdentity: ""};
    case "closeProject":
    case "shutdown":
      return null;
    case "transform":
      return transform(request.params.content);
    default:
      throw new Error("Unexpected method: " + request.method);
  }
}

process.stdin.on("data", chunk => {
  input = Buffer.concat([input, chunk]);
  while (true) {
    const headerEnd = input.indexOf("\r\n\r\n");
    if (headerEnd < 0) return;
    const header = input.subarray(0, headerEnd).toString("ascii");
    const match = /content-length:\s*(\d+)/i.exec(header);
    if (!match) throw new Error("Missing Content-Length");
    const bodyStart = headerEnd + 4;
    const bodyLength = Number(match[1]);
    if (input.length < bodyStart + bodyLength) return;
    const request = JSON.parse(input.subarray(bodyStart, bodyStart + bodyLength).toString("utf8"));
    input = input.subarray(bodyStart + bodyLength);
    if (request.id !== undefined) {
      writeMessage({jsonrpc: "2.0", id: request.id, result: handleRequest(request)});
    }
  }
});
