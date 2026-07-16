import fs from "fs";
import papa from "papaparse";
const { parse, unparse } = papa;

const cartoURL = "https://api-adresse.data.gouv.fr";

// Grenoble-ish bounding box (same as app.ts)
const southWest = [45.1, 5.6];
const northEast = [45.3, 5.9];

const targetSizes = [50, 100, 400];
const maxReqPerSec = 45; // stay under API guidance
const minDelayMs = Math.ceil(1000 / maxReqPerSec);

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function randomPoints(n) {
  return Array.from({ length: n }, () => ({
    lat: Math.random() * (northEast[0] - southWest[0]) + southWest[0],
    lng: Math.random() * (northEast[1] - southWest[1]) + southWest[1],
  }));
}

async function fetchAddresses(points) {
  const csvContent = unparse(points, { delimiter: ";" });
  const formData = new FormData();
  // Column names in the CSV
  formData.append("lon", "lng");
  formData.append("lat", "lat");
  formData.append("data", new Blob([csvContent], { type: "text/csv" }));

  const res = await fetch(`${cartoURL}/reverse/csv`, {
    method: "POST",
    body: formData,
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`API error ${res.status}: ${text.slice(0, 200)}`);
  }
  const text = await res.text();
  const parsed = parse(text.trim(), { delimiter: ";", header: true });
  return parsed.data ?? [];
}

function mapRowToAdresse(row) {
  if (!row) return null;
  const lat = Number.parseFloat(row.lat);
  const lng = Number.parseFloat(row.lng);
  if (!Number.isFinite(lat) || !Number.isFinite(lng)) return null;
  if (!row.result_name) return null;
  return {
    lat,
    lng,
    name: row.result_name,
    postCode: row.result_citycode,
    city: row.result_city,
  };
}

async function generateSet(target) {
  const adresses = [];
  let round = 0;
  while (adresses.length < target) {
    round += 1;
    const remaining = target - adresses.length;
    const batchSize = Math.min(remaining, 200);
    const points = randomPoints(batchSize);
    const rows = await fetchAddresses(points);
    const mapped = rows.map(mapRowToAdresse).filter(Boolean);
    adresses.push(...mapped);
    process.stdout.write(
      `\r${target}: ${adresses.length}/${target} (round ${round})`
    );
    await sleep(minDelayMs);
  }
  process.stdout.write("\n");
  return adresses.slice(0, target);
}

async function main() {
  for (const size of targetSizes) {
    const data = await generateSet(size);
    const outPath = `data/jeuxDeTest${size}.json`;
    fs.writeFileSync(outPath, JSON.stringify(data, null, 2));
    console.log(`Saved ${data.length} addresses to ${outPath}`);
  }
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
