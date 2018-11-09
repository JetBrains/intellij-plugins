Widget build(BuildContext context) {
  return new Scaffold(
      appBar: AppBar(
        title: Text(job.getUIName()),
      ),
      body: SingleChildScrollView(
        child: Container(
            padding: EdgeInsets.fromLTRB(16.0, 16.0, 16.0, 16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Text(
                  buildInfo.displayName,
                  style: textStyleLarge,
                ),
                Text(
                  buildInfo.getDisplayTime(),
                  style: textStyleLarge,
                ),
                Text(
                  buildInfo.getCause(),
                  style: textStyleMedium,
                // ), <---------- Missing parenthesis
                Padding(
                    padding: EdgeInsets.fromLTRB(0.0, 16.0, 0.0, 8.0),
                    child: Text(
                      "Artifacts",
                      style: textStyleMedium,
                    )),
                _renderArtifacts(context),
                _renderCommitButton(context),
                _renderSectionTitle("Feature Changelog"),
                _renderFeatureChangelog(context),
                _renderSectionTitle("Jira ChangeLog"),
                _renderJiraChangeLog(context),
              ].where((w) => w != null).toList(),
            )),
      ));
}
