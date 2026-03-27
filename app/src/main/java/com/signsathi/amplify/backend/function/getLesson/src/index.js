const { DynamoDBClient, ScanCommand } = require("@aws-sdk/client-dynamodb");
const { unmarshall } = require("@aws-sdk/util-dynamodb");

const client = new DynamoDBClient({ region: "ap-south-1" });

exports.handler = async (event) => {
  try {
    const result = await client.send(new ScanCommand({
      TableName: "signsathi-lessons"
    }));

    const items = (result.Items || []).map(i => unmarshall(i));

    // Group lessons by unitId
    const unitsMap = {};
    for (const item of items) {
      if (!unitsMap[item.unitId]) {
        unitsMap[item.unitId] = {
          unitId: item.unitId,
          unitTitle: item.unitTitle,
          unitDescription: item.unitDescription,
          unitOrder: Number(item.unitOrder),
          lessons: []
        };
      }
      unitsMap[item.unitId].lessons.push({
        lessonId: item.lessonId,
        lessonTitle: item.lessonTitle,
        lessonOrder: Number(item.lessonOrder),
        xpReward: Number(item.xpReward),
        videoUrl: item.videoUrl || "",
        thumbnailUrl: item.thumbnailUrl || ""
      });
    }

    // Sort units by unitOrder, lessons within each unit by lessonOrder
    const units = Object.values(unitsMap)
      .sort((a, b) => a.unitOrder - b.unitOrder)
      .map(u => ({
        ...u,
        lessons: u.lessons.sort((a, b) => a.lessonOrder - b.lessonOrder)
      }));

    return {
      statusCode: 200,
      headers: {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*"
      },
      body: JSON.stringify({ units })
    };

  } catch (err) {
    console.error("getLessons error:", err);
    return {
      statusCode: 500,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ error: err.message })
    };
  }
};