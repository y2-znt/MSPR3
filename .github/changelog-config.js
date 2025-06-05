module.exports = {
  preset: 'angular',
  releaseCommitMessageFormat: 'chore(release): {{currentTag}} [skip-changelog]',
  tagPrefix: 'v',
  header: '# 📋 Changelog - Plateforme OMS de Suivi des Pandémies\n\nTous les changements notables de ce projet seront documentés dans ce fichier.\n\nLe format est basé sur [Keep a Changelog](https://keepachangelog.com/fr/1.0.0/),\net ce projet adhère au [Semantic Versioning](https://semver.org/lang/fr/).\n\n',
  types: [
    {
      type: 'feat',
      section: '🚀 Nouvelles fonctionnalités',
      hidden: false
    },
    {
      type: 'fix',
      section: '🐛 Corrections de bugs',
      hidden: false
    },
    {
      type: 'perf',
      section: '⚡ Améliorations de performance',
      hidden: false
    },
    {
      type: 'refactor',
      section: '♻️ Refactoring',
      hidden: false
    },
    {
      type: 'docs',
      section: '📚 Documentation',
      hidden: false
    },
    {
      type: 'style',
      section: '💄 Style et formatage',
      hidden: false
    },
    {
      type: 'test',
      section: '✅ Tests',
      hidden: false
    },
    {
      type: 'build',
      section: '🔧 Build et CI/CD',
      hidden: false
    },
    {
      type: 'ci',
      section: '👷 Intégration continue',
      hidden: false
    },
    {
      type: 'chore',
      section: '🔨 Maintenance',
      hidden: false
    },
    {
      type: 'revert',
      section: '⏪ Annulations',
      hidden: false
    },
    // Types spécifiques au projet MSPR3
    {
      type: 'etl',
      section: '🔄 Processus ETL',
      hidden: false
    },
    {
      type: 'ai',
      section: '🤖 Intelligence Artificielle',
      hidden: false
    },
    {
      type: 'data',
      section: '📊 Gestion des données',
      hidden: false
    },
    {
      type: 'api',
      section: '🌐 API',
      hidden: false
    },
    {
      type: 'ui',
      section: '🎨 Interface utilisateur',
      hidden: false
    },
    {
      type: 'security',
      section: '🔒 Sécurité',
      hidden: false
    },
    {
      type: 'config',
      section: '⚙️ Configuration',
      hidden: false
    }
  ],
  commitUrlFormat: '{{host}}/{{owner}}/{{repository}}/commit/{{hash}}',
  compareUrlFormat: '{{host}}/{{owner}}/{{repository}}/compare/{{previousTag}}...{{currentTag}}',
  issueUrlFormat: '{{host}}/{{owner}}/{{repository}}/issues/{{id}}',
  userUrlFormat: '{{host}}/{{user}}',
  releaseCommitMessageFormat: 'chore(release): {{currentTag}}',
  issuePrefixes: ['#'],
  noteGroupsSort: 'title',
  notesSort: compareFunc,
  transform: (commit, context) => {
    let discard = true;
    const issues = [];

    commit.notes.forEach(note => {
      note.title = 'BREAKING CHANGES';
      discard = false;
    });

    // Mapping des scopes spécifiques au projet
    const scopeMapping = {
      'backend': '🏗️ Backend',
      'frontend': '🎨 Frontend', 
      'ai-api': '🤖 API IA',
      'ai-training': '🧠 Entraînement IA',
      'etl': '🔄 ETL',
      'database': '🗄️ Base de données',
      'docker': '🐳 Docker',
      'ci': '👷 CI/CD',
      'docs': '📚 Documentation',
      'config': '⚙️ Configuration',
      'security': '🔒 Sécurité',
      'performance': '⚡ Performance'
    };

    if (commit.scope && scopeMapping[commit.scope]) {
      commit.scope = scopeMapping[commit.scope];
    }

    if (commit.type === 'feat') {
      commit.type = '🚀 Nouvelles fonctionnalités';
    } else if (commit.type === 'fix') {
      commit.type = '🐛 Corrections de bugs';
    } else if (commit.type === 'perf') {
      commit.type = '⚡ Améliorations de performance';
    } else if (commit.type === 'revert' || commit.revert) {
      commit.type = '⏪ Annulations';
    } else if (discard) {
      return;
    } else if (commit.type === 'docs') {
      commit.type = '📚 Documentation';
    } else if (commit.type === 'style') {
      commit.type = '💄 Style et formatage';
    } else if (commit.type === 'refactor') {
      commit.type = '♻️ Refactoring';
    } else if (commit.type === 'test') {
      commit.type = '✅ Tests';
    } else if (commit.type === 'build') {
      commit.type = '🔧 Build et CI/CD';
    } else if (commit.type === 'ci') {
      commit.type = '👷 Intégration continue';
    } else if (commit.type === 'chore') {
      commit.type = '🔨 Maintenance';
    }

    if (commit.scope === '*') {
      commit.scope = '';
    }

    if (typeof commit.hash === 'string') {
      commit.shortHash = commit.hash.substring(0, 7);
    }

    if (typeof commit.subject === 'string') {
      let url = context.repository
        ? `${context.host}/${context.owner}/${context.repository}`
        : context.repoUrl;
      if (url) {
        url = `${url}/issues/`;
        commit.subject = commit.subject.replace(/#([0-9]+)/g, (_, issue) => {
          issues.push(issue);
          return `[#${issue}](${url}${issue})`;
        });
      }
      if (context.host) {
        commit.subject = commit.subject.replace(/\B@([a-z0-9](?:-?[a-z0-9/]){0,38})/g, (_, username) => {
          if (username.includes('/')) {
            return `@${username}`;
          }
          return `[@${username}](${context.host}/${username})`;
        });
      }
    }

    commit.references.forEach(reference => {
      if (reference.issue && issues.indexOf(reference.issue) === -1) {
        issues.push(reference.issue);
      }
    });

    return commit;
  }
};

function compareFunc(a, b) {
  if (a.scope && b.scope) {
    if (a.scope === b.scope) return a.subject.localeCompare(b.subject);
    return a.scope.localeCompare(b.scope);
  }
  if (a.scope) return -1;
  if (b.scope) return 1;
  return a.subject.localeCompare(b.subject);
}
