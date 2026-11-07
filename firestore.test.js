const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read cars", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("cars").get());
});

test("Authenticated user: can create and read own car", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const carDoc = aliceDb.collection("users").doc(ALICE_UID).collection("cars").doc("car_1");
  await assertSucceeds(
    carDoc.set({
      userId: ALICE_UID,
      name: "Dacia Logan",
      plate: "14285-A-26",
      status: "AVAILABLE",
      year: 2023,
      dailyPrice: 300,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(carDoc.get());
});

test("Cross-user isolation: Bob cannot read Alice's cars", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("cars").doc("car_1").set({
      userId: ALICE_UID,
      name: "Dacia Logan",
      plate: "14285-A-26",
      status: "AVAILABLE",
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("cars").doc("car_1").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("cars").get());
});

test("Validation: rejects invalid car status", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const carDoc = aliceDb.collection("users").doc(ALICE_UID).collection("cars").doc("car_invalid");
  await assertFails(
    carDoc.set({
      userId: ALICE_UID,
      name: "Dacia Logan",
      plate: "14285-A-26",
      status: "INVALID_STATUS",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: can create and query own bookings", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bkDoc = aliceDb.collection("users").doc(ALICE_UID).collection("bookings").doc("bk_1");
  await assertSucceeds(
    bkDoc.set({
      userId: ALICE_UID,
      carId: "car_1",
      clientName: "Yassine El Amrani",
      pickupDate: "2026-11-10",
      returnDate: "2026-11-15",
      status: "RESERVED",
      totalPrice: 1500,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("bookings").get());
});
