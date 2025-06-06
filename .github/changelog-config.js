module.exports = {
  preset: 'angular',
  header: '# Changelog - WHO Pandemic Surveillance Platform\n\nAll notable changes to this project will be documented in this file.\n\nThe format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),\nand this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).\n\n',
  transform: function(commit, context) {
    const scopeMapping = {
      'backend': 'Backend',
      'frontend': 'Frontend', 
      'ai-api': 'AI API',
      'ai-training': 'AI Training',
      'etl': 'ETL',
      'database': 'Database',
      'docker': 'Docker',
      'ci': 'CI/CD',
      'docs': 'Documentation',
      'config': 'Configuration',
      'security': 'Security',
      'performance': 'Performance'
    };

    const typeMapping = {
      feat: 'New Features',
      fix: 'Bug Fixes',
      perf: 'Performance Improvements',
      refactor: 'Code Refactoring',
      docs: 'Documentation',
      style: 'Styles',
      test: 'Tests',
      build: 'Build System',
      ci: 'Continuous Integration',
      chore: 'Chores',
      revert: 'Reverts'
    };

    if (!typeMapping[commit.type]) {
      return null;
    }

    if (commit.scope && scopeMapping[commit.scope]) {
      commit.scope = scopeMapping[commit.scope];
    }

    commit.type = typeMapping[commit.type];

    return commit;
  },
  writerOpts: {
    groupBy: 'type',
    commitGroupsSort: 'title',
    commitsSort: ['scope', 'subject'],
    noteGroupsSort: 'title'
  }
};