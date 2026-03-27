const { DynamoDBClient, QueryCommand, PutItemCommand } = require("@aws-sdk/client-dynamodb");
const { unmarshall, marshall } = require("@aws-sdk/util-dynamodb");

const client = new DynamoDBClient({ region: "ap-south-1" });

exports.handler = async (event) => {
  // Cognito puts the userId (sub) in the request context
  const userId =
    event.requestContext?.authorizer?.claims?.sub ||
    event.requestContext?.identity?.cognitoIdentityId;

  if (!userId) {
    return {
      statusCode: 401,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ error: "Unauthorized" })
    };
  }

  try {
    const result = await client.send(new QueryCommand({
      TableName: "signsathi-user-progress",
      KeyConditionExpression: "userId = :uid",
      ExpressionAttributeValues: { ":uid": { S: userId } }
    }));

    const items = (result.Items || []).map(i => unmarshall(i));

    // STATS row holds top-level XP, streak, hearts
    const statsRow = items.find(i => i.lessonId === "STATS");

    // If no STATS row yet, create a default one for this new user
    if (!statsRow) {
      await client.send(new PutItemCommand({
        TableName: "signsathi-user-progress",
        Item: marshall({
          userId,
          lessonId: "STATS",
          xp: 0,
          streakDays: 0,
          heartsLeft: 5,
          lastActivityDate: ""
        })
      }));
    }

    // Per-lesson progress map: { lessonId -> "completed" | "active" }
    const lessonProgress = {};
    for (const item of items) {
      if (item.lessonId !== "STATS") {
        lessonProgress[item.lessonId] = item.status;
      }
    }

    return {
      statusCode: 200,
      headers: {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*"
      },
      body: JSON.stringify({
        xp: statsRow?.xp ?? 0,
        streakDays: statsRow?.streakDays ?? 0,
        heartsLeft: statsRow?.heartsLeft ?? 5,
        lessonProgress
      })
    };

  } catch (err) {
    console.error("getUserProgress error:", err);
    return {
      statusCode: 500,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ error: err.message })
    };
  }
};