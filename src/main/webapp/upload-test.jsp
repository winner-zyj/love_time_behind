<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Upload Test</title>
</head>
<body>
    <h2>Heart Wall Photo Upload Test</h2>
    <form action="api/heart-wall/photos" method="post" enctype="multipart/form-data">
        <input type="hidden" name="projectId" value="1">
        <input type="hidden" name="positionIndex" value="1">
        <label for="file">Select photo:</label>
        <input type="file" id="file" name="file" accept="image/*" required><br><br>
        <input type="submit" value="Upload Photo">
    </form>

    <h2>User Avatar Upload Test</h2>
    <form action="api/user/avatar/upload" method="post" enctype="multipart/form-data">
        <label for="avatar">Select avatar:</label>
        <input type="file" id="avatar" name="file" accept="image/*" required><br><br>
        <input type="submit" value="Upload Avatar">
    </form>

    <h2>Challenge Photo Upload Test</h2>
    <form action="api/challenge/upload" method="post" enctype="multipart/form-data">
        <label for="challenge">Select photo:</label>
        <input type="file" id="challenge" name="file" accept="image/*" required><br><br>
        <input type="submit" value="Upload Photo">
    </form>
</body>
</html>